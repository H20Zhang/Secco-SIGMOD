package org.apache.spark.secco.expression

import java.util.UUID
import org.apache.spark.secco.execution.InternalRow
import org.apache.spark.secco.trees.TreeNode
import org.apache.spark.secco.types.{AbstractDataType, DataType, LongType}
import org.apache.spark.util.Utils

abstract class Expression extends TreeNode[Expression] {

  def foldable: Boolean = false

  def deterministic: Boolean = children.forall(_.deterministic)

  def nullable: Boolean

  def references: AttributeSet =
    AttributeSet(children.flatMap(_.references))

  def eval(input: InternalRow): Any

//  def genCode() = ???
//  def doGenCode() = ???

  /**
    * Returns `true` if this expression and all its children have been resolved to a specific schema
    * and input data types checking passed, and `false` if it still contains any unresolved
    * placeholders or has data types mismatch.
    * Implementations of expressions should override this if the resolution of this type of
    * expression involves more than just the resolution of its children and type checking.
    */
  lazy val resolved: Boolean = childrenResolved
//  && checkInputDataTypes().isSuccess

  /**
    * Returns the [[DataType]] of the result of evaluating this expression.  It is
    * invalid to query the dataType of an unresolved expression (i.e., when `resolved` == false).
    */
  def dataType: DataType

  /**
    * Returns true if  all the children of this expression have been resolved to a specific schema
    * and false if any still contains any unresolved placeholders.
    */
  def childrenResolved: Boolean = children.forall(_.resolved)

//  /**
//    * Returns an expression where a best effort attempt has been made to transform `this` in a way
//    * that preserves the result but removes cosmetic variations (case sensitivity, ordering for
//    * commutative operations, etc.)  See [[Canonicalize]] for more details.
//    *
//    * `deterministic` expressions where `this.canonicalized == other.canonicalized` will always
//    * evaluate to the same result.
//    */
//  lazy val canonicalized: Expression = {
//    val canonicalizedChildren = children.map(_.canonicalized)
//    Canonicalize.execute(withNewChildren(canonicalizedChildren))
//  }

//  /**
//    * Returns true when two expressions will always compute the same result, even if they differ
//    * cosmetically (i.e. capitalization of names in attributes may be different).
//    *
//    * See [[Canonicalize]] for more details.
//    */
//  def semanticEquals(other: Expression): Boolean =
//    deterministic && other.deterministic && canonicalized == other.canonicalized

//  /**
//    * Returns a `hashCode` for the calculation performed by this expression. Unlike the standard
//    * `hashCode`, an attempt has been made to eliminate cosmetic differences.
//    *
//    * See [[Canonicalize]] for more details.
//    */
//  def semanticHash(): Int = canonicalized.hashCode()

//  /**
//    * Checks the input data types, returns `TypeCheckResult.success` if it's valid,
//    * or returns a `TypeCheckResult` with an error message if invalid.
//    * Note: it's not valid to call this method until `childrenResolved == true`.
//    */
//  def checkInputDataTypes(): TypeCheckResult = TypeCheckResult.TypeCheckSuccess

  /**
    * Returns a user-facing string representation of this expression's name.
    * This should usually match the name of the function in SQL.
    */
  def prettyName: String = nodeName

  protected def flatArguments: Iterator[Any] =
    productIterator.flatMap {
      case t: Traversable[_] => t
      case single            => single :: Nil
    }

  // Marks this as final, Expression.verboseString should never be called, and thus shouldn't be
  // overridden by concrete classes.
  final override def verboseString: String = simpleString

  override def simpleString: String = toString

  override def toString: String =
    prettyName + Utils.truncatedString(flatArguments.toSeq, "(", ", ", ")")

  /**
    * Returns SQL representation of this expression.  For expressions extending [[NonSQLExpression]],
    * this method may return an arbitrary user facing string.
    */
  def sql: String = {
    val childrenSQL = children.map(_.sql).mkString(", ")
    s"$prettyName($childrenSQL)"
  }

}

abstract class LeafExpression extends Expression {
  override final def children: Seq[Expression] = Nil
}

abstract class UnaryExpression extends Expression {
  def child: Expression

  override final def children: Seq[Expression] = child :: Nil

  override def foldable: Boolean = child.foldable
  override def nullable: Boolean = child.nullable

  /**
    * Default behavior of evaluation according to the default nullability of UnaryExpression.
    * If subclass of UnaryExpression override nullable, probably should also override this.
    */
  override def eval(input: InternalRow): Any = {
    val value = child.eval(input)
    if (value == null) {
      null
    } else {
      nullSafeEval(value)
    }
  }

  /**
    * Called by default [[eval]] implementation.  If subclass of UnaryExpression keep the default
    * nullability, they can override this method to save null-check code.  If we need full control
    * of evaluation process, we should override [[eval]].
    */
  protected def nullSafeEval(input: Any): Any =
    sys.error(s"UnaryExpressions must override either eval or nullSafeEval")
}

abstract class BinaryExpression extends Expression {
  def left: Expression
  def right: Expression

  override final def children: Seq[Expression] = Seq(left, right)

  override def foldable: Boolean = left.foldable && right.foldable

  override def nullable: Boolean = left.nullable || right.nullable

  /**
    * Default behavior of evaluation according to the default nullability of BinaryExpression.
    * If subclass of BinaryExpression override nullable, probably should also override this.
    */
  override def eval(input: InternalRow): Any = {
    val value1 = left.eval(input)
    if (value1 == null) {
      null
    } else {
      val value2 = right.eval(input)
      if (value2 == null) {
        null
      } else {
        nullSafeEval(value1, value2)
      }
    }
  }

  /**
    * Called by default [[eval]] implementation.  If subclass of BinaryExpression keep the default
    * nullability, they can override this method to save null-check code.  If we need full control
    * of evaluation process, we should override [[eval]].
    */
  protected def nullSafeEval(input1: Any, input2: Any): Any =
    sys.error(s"BinaryExpressions must override either eval or nullSafeEval")
}

/**
  * TODO: add type check
  *
  * A [[BinaryExpression]] that is an operator, with two properties:
  *
  * 1. The string representation is "x symbol y", rather than "funcName(x, y)".
  * 2. Two inputs are expected to be of the same type. If the two inputs have different types,
  *    the analyzer will find the tightest common type and do the proper type casting.
  */
abstract class BinaryOperator extends BinaryExpression {

  /**
    * Expected input type from both left/right child expressions, similar to the
    * [[ImplicitCastInputTypes]] trait.
    */
  def inputType: AbstractDataType

  def symbol: String

  def sqlOperator: String = symbol

  override def toString: String = s"($left $symbol $right)"

  def inputTypes: Seq[AbstractDataType] = Seq(inputType, inputType)

  override def sql: String = s"(${left.sql} $sqlOperator ${right.sql})"

}

/**
  * An expression that cannot be evaluated. Some expressions don't live past analysis or optimization
  * time (e.g. Star). This trait is used by those expressions.
  */
trait Unevaluable extends Expression {

  final override def eval(input: InternalRow = null): Any =
    throw new UnsupportedOperationException(
      s"Cannot evaluate expression: $this"
    )
}

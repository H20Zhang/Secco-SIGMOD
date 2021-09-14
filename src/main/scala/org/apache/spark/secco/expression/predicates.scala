package org.apache.spark.secco.expression

import org.apache.spark.secco.expression.{
  BinaryOperator,
  Expression,
  UnaryExpression
}
import org.apache.spark.secco.execution.InternalRow
import org.apache.spark.secco.types.{
  AbstractDataType,
  AnyDataType,
  BooleanType,
  DataType
}

/**
  * An [[Expression]] that returns a boolean value.
  */
trait Predicate extends Expression {
  override def dataType: DataType = BooleanType
}

case class Not(child: Expression) extends UnaryExpression with Predicate {
  override def toString: String = s"NOT $child"

  override def sql: String = s"(NOT ${child.sql})"
}

abstract class BinaryComparison extends BinaryOperator with Predicate {}

object BinaryComparison {
  def unapply(e: BinaryComparison): Option[(Expression, Expression)] =
    Some((e.left, e.right))
}

case class EqualTo(left: Expression, right: Expression)
    extends BinaryComparison {
  override def inputType: AbstractDataType = AnyDataType

  override def symbol: String = "="
}

case class GreaterThan(left: Expression, right: Expression)
    extends BinaryComparison {
  override def inputType: AbstractDataType = AnyDataType

  override def symbol: String = ">"
}

case class GreaterThanOrEqual(left: Expression, right: Expression)
    extends BinaryComparison {
  override def inputType: AbstractDataType = AnyDataType

  override def symbol: String = "=>"
}

case class LessThan(left: Expression, right: Expression)
    extends BinaryComparison {
  override def inputType: AbstractDataType = AnyDataType

  override def symbol: String = "<"
}

case class LessThanOrEqual(left: Expression, right: Expression)
    extends BinaryComparison {
  override def inputType: AbstractDataType = AnyDataType

  override def symbol: String = "<="
}

case class And(left: Expression, right: Expression) extends BinaryComparison {
  override def inputType: AbstractDataType = AnyDataType

  override def symbol: String = "and"
}

case class Or(left: Expression, right: Expression) extends BinaryComparison {
  override def inputType: AbstractDataType = AnyDataType

  override def symbol: String = "or"
}

case class In(value: Expression, list: Seq[Expression]) extends Predicate {
  require(list != null, "list should not be null")

  override def children: Seq[Expression] = value +: list

  override def nullable: Boolean = children.exists(_.nullable)
  override def foldable: Boolean = children.forall(_.foldable)

  override def toString: String = s"$value IN ${list.mkString("(", ",", ")")}"

  override def eval(input: InternalRow): Any = ???
}

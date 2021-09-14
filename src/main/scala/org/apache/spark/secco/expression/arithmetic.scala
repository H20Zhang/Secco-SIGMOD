package org.apache.spark.secco.expression

import org.apache.spark.secco.expression.{BinaryOperator, Expression}
import org.apache.spark.secco.types.{AbstractDataType, DataType, NumericType}

case class UnaryPositive(child: Expression) extends UnaryExpression {
  override def prettyName: String = "positive"

  /**
    * Returns the [[DataType]] of the result of evaluating this expression.  It is
    * invalid to query the dataType of an unresolved expression (i.e., when `resolved` == false).
    */
  override def dataType: DataType = NumericType

  override def sql: String = s"(+ ${child.sql})"
}

case class UnaryMinus(child: Expression) extends UnaryExpression {

  override def toString: String = s"-$child"

  /**
    * Returns the [[DataType]] of the result of evaluating this expression.  It is
    * invalid to query the dataType of an unresolved expression (i.e., when `resolved` == false).
    */
  override def dataType: DataType = NumericType

  override def sql: String = s"(- ${child.sql})"
}

abstract class BinaryArithmetic extends BinaryOperator {
  override def dataType: DataType = left.dataType
}

object BinaryArithmetic {
  def unapply(e: BinaryArithmetic): Option[(Expression, Expression)] =
    Some((e.left, e.right))
}

case class Add(left: Expression, right: Expression) extends BinaryArithmetic {
  override def symbol: String = "+"

  /**
    * Expected input type from both left/right child expressions
    */
  override def inputType: AbstractDataType = NumericType
}

case class Subtract(left: Expression, right: Expression)
    extends BinaryArithmetic {
  override def symbol: String = "-"

  /**
    * Expected input type from both left/right child expressions
    */
  override def inputType: AbstractDataType = NumericType
}

case class Multiply(left: Expression, right: Expression)
    extends BinaryArithmetic {
  override def symbol: String = "*"

  /**
    * Expected input type from both left/right child expressions
    */
  override def inputType: AbstractDataType = NumericType
}

case class Divide(left: Expression, right: Expression)
    extends BinaryArithmetic {
  override def symbol: String = "/"

  /**
    * Expected input type from both left/right child expressions
    */
  override def inputType: AbstractDataType = NumericType
}

case class Remainder(left: Expression, right: Expression)
    extends BinaryArithmetic {
  override def symbol: String = "%"

  /**
    * Expected input type from both left/right child expressions
    */
  override def inputType: AbstractDataType = NumericType
}

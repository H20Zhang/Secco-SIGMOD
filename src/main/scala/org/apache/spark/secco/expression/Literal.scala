package org.apache.spark.secco.expression

import org.apache.spark.secco.execution.InternalRow
import org.apache.spark.secco.types._

import java.util.Objects

case class Literal(value: Any, dataType: DataType) extends LeafExpression {

  override def foldable: Boolean = true

  override def nullable: Boolean = value == null

  override def eval(input: InternalRow): Any = value

  override def toString: String =
    value match {
      case null  => "null"
      case other => other.toString
    }

  override def hashCode(): Int = {
    val valueHashCode = value match {
      case null  => 0
      case other => other.hashCode()
    }
    31 * Objects.hashCode(dataType) + valueHashCode
  }

  override def equals(other: Any): Boolean =
    other match {
      case o: Literal if !dataType.equals(o.dataType) => false
      case o: Literal =>
        (value, o.value) match {
          case (null, null) => true
          case (a, b)       => a != null && a.equals(b)
        }
      case _ => false
    }

  override def sql: String =
    (value, dataType) match {
      case v: (Int, IntegerType)   => v._1.toString
      case v: (Long, LongType)     => v._2 + "L"
      case v: (Float, FloatType)   => v._2 + "f"
      case v: (Double, DoubleType) => v._2.toString
      case v: (String, StringType) => v._1
    }

}

object Literal {
  val TrueLiteral: Literal = Literal(true, BooleanType)

  val FalseLiteral: Literal = Literal(false, BooleanType)

  def apply(v: Any): Literal =
    v match {
      case i: Int     => Literal(i, IntegerType)
      case l: Long    => Literal(l, LongType)
      case d: Double  => Literal(d, DoubleType)
      case f: Float   => Literal(f, FloatType)
      case s: String  => Literal(s, StringType)
      case b: Boolean => Literal(b, BooleanType)
      case v: Literal => v
      case _ =>
        throw new RuntimeException(
          "Unsupported literal type " + v.getClass + " " + v
        )
    }
}

package org.apache.spark.secco.types

import java.util.Locale

//TODO: construct the type system for Secco for more efficient data retrieval and storage
abstract class AbstractDataType {

  /** Readable string representation for the type. */
  def simpleString: String

}

abstract class DataType extends AbstractDataType {

  /** Name of the type used in JSON serialization. */
  def typeName: String = {
    this.getClass.getSimpleName
      .stripSuffix("$")
      .stripSuffix("Type")
      .stripSuffix("UDT")
      .toLowerCase(Locale.ROOT)
  }

  /** Readable string representation for the type. */
  def simpleString: String = typeName
}

class BooleanType extends DataType
class IntegerType extends DataType
class LongType extends DataType
class FloatType extends DataType
class DoubleType extends DataType
class StringType extends DataType
class AnyDataType extends DataType
class NumericType extends DataType

case object BooleanType extends BooleanType
case object IntegerType extends IntegerType
case object LongType extends LongType
case object FloatType extends FloatType
case object DoubleType extends DoubleType
case object StringType extends StringType
case object AnyDataType extends AnyDataType
case object NumericType extends NumericType

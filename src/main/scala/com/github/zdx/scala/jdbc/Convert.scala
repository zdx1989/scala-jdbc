package com.github.zdx.scala.jdbc

import java.sql._

import shapeless.Generic
/**
  * Created by zhoudunxiong on 2018/4/26.
  */
trait Convert[A] {

  def toList(a: A): List[Any]
}

object Convert {

  def apply[A](implicit ca: Convert[A]) = ca

  def instance[A](f: A => List[Any]): Convert[A] = new Convert[A] {
    override def toList(a: A): List[Any] = f(a)
  }

  implicit val intConvert: Convert[Int] = instance(List(_))

  implicit val longConvert: Convert[Long] = instance(List(_))

  implicit val doubleConvert: Convert[Double] = instance(List(_))

  implicit val short: Convert[Short] = instance(List(_))

  implicit val float: Convert[Float] = instance(List(_))

  implicit val timestampConvert: Convert[Timestamp] = instance(List(_))

  implicit val dateConvert: Convert[Date] = instance(List(_))

  implicit val timeConvert: Convert[Time] = instance(List(_))

  implicit val stringConvert: Convert[String] = instance(List(_))

  implicit val byteConvert: Convert[Byte] = instance(List(_))

  implicit val booleanConvert: Convert[Boolean] = instance(List(_))

  import shapeless.{HNil, HList, ::}

  implicit val hNilConvert: Convert[HNil] = instance(hNil => Nil)

  implicit def hListConvert[T, H <: HList](implicit
                                           tConv: Convert[T],
                                           hConv: Convert[H]): Convert[T :: H] =
    instance {
      case head :: tail => tConv.toList(head) ++ hConv.toList(tail)
    }

  implicit def genericConvert[A, R](implicit
                                    gen: Generic.Aux[A, R],
                                    rConv: Convert[R]): Convert[A] =
    instance(a => rConv.toList(gen.to(a)))

}

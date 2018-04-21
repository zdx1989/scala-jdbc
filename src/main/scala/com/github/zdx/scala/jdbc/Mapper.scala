package com.github.zdx.scala.jdbc

import java.sql._

import shapeless.Generic

/**
  * Created by zhoudunxiong on 2018/4/21.
  */
sealed trait Mapper[A]

trait TypeMapper[A] extends Mapper[A] {

  def to(rs: ResultSet, index: Int): A
}

trait RawFieldMapper[A] extends Mapper[A] {

  def to(rs: ResultSet, indexes: Seq[Int]): A
}


object Mapper {

  def apply[A](implicit mapper: Mapper[A]) = mapper

  def typeMapper[A](f: (ResultSet, Int) => A): TypeMapper[A] = new TypeMapper[A] {
    override def to(rs: ResultSet, index: Int): A = f(rs, index)
  }

  def rawFieldMapper[A](f: (ResultSet, Seq[Int]) => A): RawFieldMapper[A] = new RawFieldMapper[A] {
    override def to(rs: ResultSet, indexes: Seq[Int]): A = f(rs, indexes)
  }

  implicit val intMapper: TypeMapper[Int] = typeMapper(_.getInt(_))

  implicit val longMapper: TypeMapper[Long] = typeMapper(_.getLong(_))

  implicit val doubleMapper: TypeMapper[Double] = typeMapper(_.getDouble(_))

  implicit val short: TypeMapper[Short] = typeMapper(_.getShort(_))

  implicit val float: TypeMapper[Float] = typeMapper(_.getFloat(_))

  implicit val timestampMapper: TypeMapper[Timestamp] = typeMapper(_.getTimestamp(_))

  implicit val dateMapper: TypeMapper[Date] = typeMapper(_.getDate(_))

  implicit val timeMapper: TypeMapper[Time] = typeMapper(_.getTime(_))

  implicit val stringMapper: TypeMapper[String] = typeMapper(_.getString(_))

  implicit val byteMapper: TypeMapper[Byte] = typeMapper(_.getByte(_))

  implicit val booleanMapper: TypeMapper[Boolean] = typeMapper(_.getBoolean(_))

  import shapeless.{HNil, HList, ::}

  implicit val hNilMapper: RawFieldMapper[HNil] = rawFieldMapper { (rs, indexes) =>
    if (indexes.isEmpty) HNil
    else throw new IllegalArgumentException(s"cannot be converted to HNil")
  }

  implicit def hListMapper[T, H <: HList](implicit
                                          tMapper: TypeMapper[T],
                                          hMapper: RawFieldMapper[H]): RawFieldMapper[T :: H] =
    rawFieldMapper { (rs, indexes) =>
      indexes match {
        case Nil => throw new IllegalArgumentException(s"The empty ResultSet cannot be converted to HList")
        case _ => tMapper.to(rs, indexes.head) :: hMapper.to(rs, indexes.tail)
      }
    }

  implicit def generic[A, R](implicit
                             gen: Generic.Aux[A, R],
                             rMapper: RawFieldMapper[R]): RawFieldMapper[A] =
    rawFieldMapper { (rs, indexes) =>
      gen.from(rMapper.to(rs, indexes))
    }
}

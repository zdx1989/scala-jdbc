package com.github.zdx.scala.jdbc

import java.sql._

import IOUtils._

/**
  * Created by zhoudunxiong on 2018/4/21.
  */
case class StatementIO(sql: String, params: Any*) {

  import StatementIO._

  def query[A](implicit mapper: RawFieldMapper[A]): ConnectionIO[Seq[A]] = ConnectionIO { conn =>
    using(conn.prepareStatement(sql)) { stmt =>
      params.zipWithIndex.foreach {
        case (p, i) => set(stmt, i, p)
      }
      using(stmt.executeQuery()) { rs =>
        def loop(rs: ResultSet, lines: Seq[A]): Seq[A] =
          if (rs.next()) {
            val a = mapper.to(rs, 1 to rs.getMetaData.getColumnCount)
            loop(rs, lines :+ a)
          } else lines
        loop(rs, Nil)
      }
    }
  }

  def update: ConnectionIO[Int] = ConnectionIO { conn =>
    using(conn.prepareStatement(sql)) { stmt =>
      params.zipWithIndex.foreach {
        case (p, i) => set(stmt, i + 1, p)
      }
      stmt.executeUpdate()
    }
  }

}

object StatementIO {

  implicit class SqlStringInterpolation(val sc: StringContext) extends AnyVal {
    def sql(args: Any*): StatementIO = {
      val sql = sc.parts.mkString("?")
      StatementIO(sql, args:_*)
    }
  }

  def set(stmt: PreparedStatement, index: Int, value: Any): Unit = value match {
    case v: Int => stmt.setInt(index, v)
    case v: Long => stmt.setLong(index, v)
    case v: Double => stmt.setDouble(index, v)
    case v: Short => stmt.setShort(index, v)
    case v: Float => stmt.setFloat(index, v)
    case v: Timestamp => stmt.setTimestamp(index, v)
    case v: Date => stmt.setDate(index, v)
    case v: Time => stmt.setTime(index, v)
    case v: String => stmt.setString(index, v)
    case v: Byte => stmt.setByte(index, v)
    case v: Boolean => stmt.setBoolean(index, v)
    case _ => throw new UnsupportedOperationException(s"Unsupported type: ${value.getClass.getName}")
  }
}

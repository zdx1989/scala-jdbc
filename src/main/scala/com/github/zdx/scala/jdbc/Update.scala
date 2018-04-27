package com.github.zdx.scala.jdbc

import IOUtils._

/**
  * Created by zhoudunxiong on 2018/4/26.
  */
trait Update[A] {

  import StatementIO._

  val sql: String

  def updateMany(sa: Seq[A])(implicit ca: Convert[A]): ConnectionIO[Seq[Int]] = ConnectionIO { conn =>
    using(conn.prepareStatement(sql)) { stmt =>
      sa.foreach { a =>
        val params = ca.toList(a)
        params.zipWithIndex.foreach {
          case (p, i) => set(stmt, i + 1, p)
        }
        stmt.addBatch()
      }
      val arr = stmt.executeBatch()
      stmt.clearBatch()
      arr
    }
  }

}

object Update {

  def apply[A](s: String): Update[A] = new Update[A] {
    override val sql: String = s
  }
}

package com.github.zdx.scala.jdbc

import java.sql.Connection

/**
  * Created by zhoudunxiong on 2018/4/21.
  */
trait ConnectionIO[A] {

  val func: Connection => A

  def map[B](f: A => B): ConnectionIO[B] = ConnectionIO { conn =>
    f(func(conn))
  }

  def flatMap[B](f: A => ConnectionIO[B]): ConnectionIO[B] = ConnectionIO { conn =>
    f(func(conn)).func(conn)
  }

  def run(conn: Connection): Either[Exception, A] =
    try Right(func(conn))
    catch { case e: Exception => Left(e) }
    finally conn.close()

  def transact(conn: Connection): Either[Exception, A] = {
    conn.setAutoCommit(false)
    try Right(func(conn))
    catch {
      case e: Exception =>
        conn.rollback()
        Left(e)
    } finally conn.close()
  }




}

object ConnectionIO {

  def apply[A](f: Connection => A): ConnectionIO[A] = new ConnectionIO[A] {
    override val func: (Connection) => A = f
  }

  implicit class ConnectionIOSeq[A](csa: ConnectionIO[Seq[A]]) {

    def list: ConnectionIO[List[A]] = csa.map(_.toList)

    def unique: ConnectionIO[A] = csa.map(_.head)

    def option: ConnectionIO[Option[A]] = csa.map(_.headOption)
  }
}

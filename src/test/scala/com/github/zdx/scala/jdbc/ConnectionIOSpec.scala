package com.github.zdx.scala.jdbc

import java.sql.{Connection, DriverManager}

import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import StatementIO._

/**
  * Created by zhoudunxiong on 2018/4/21.
  */
class ConnectionIOSpec extends FunSpec with Matchers with BeforeAndAfter {

  case class Student(id: Long, name: String, score: Int)

  def conn: Connection = {
    Class.forName("org.h2.Driver")
    DriverManager.getConnection("jdbc:h2:~/test")
  }

  before {
    sql"""DROP TABLE IF EXISTS student""".update.run(conn)
    sql"""CREATE TABLE student (
        id serial PRIMARY KEY,
        name varchar (50) NOT NULL,
        score int
      )""".update.run(conn)
  }

  after {
    sql"""DROP TABLE IF EXISTS student""".update.run(conn)
  }

  describe("Test ConnectionIO") {

    it("should return List[Student]") {
      val s1 = Student(1, "zdx", 80)
      val s2 = Student(2, "ygy", 100)
      sql"INSERT INTO student (name, score) VALUES (${s1.name}, ${s1.score})".update.run(conn)
      sql"INSERT INTO student (name, score) VALUES (${s2.name}, ${s2.score})".update.run(conn)
      val students: Either[Exception, List[Student]] =
        sql"SELECT * FROM student".query[Student].list.run(conn)
      students should be (Right(List(s1, s2)))
    }

    it("should return Option[Student]") {
      val s1 = Student(1, "zdx", 80)
      sql"INSERT INTO student (name, score) VALUES (${s1.name}, ${s1.score})".update.run(conn)
      val student: Either[Exception, Option[Student]] =
        sql"SELECT * FROM student".query[Student].option.run(conn)
      student should be (Right(Some(s1)))
    }

    it("should return Student") {
      val s1 = Student(1, "ygy", 100)
      sql"INSERT INTO student (name, score) VALUES (${s1.name}, ${s1.score})".update.runUnsafe(conn)
      val student = sql"SELECT * FROM student".query[Student].unique
      student.run(conn) should be (Right(s1))
      student.runUnsafe(conn) should be (s1)
      val tuple: Either[Exception, (Long, String, Int)] =
        sql"SELECT * FROM student".query[(Long, String, Int)].unique.run(conn)
      tuple should be (Right((1, "ygy", 100)))
    }

    it("transaction") {
      val s1 = Student(1, "zdx", 80)
      val s2 = Student(2, "ygy", 100)
      val insertUsers: ConnectionIO[Seq[Int]] = for {
        i <- sql"INSERT INTO student (name, score) VALUES (${s1.name}, ${s1.score})".update
        j <- sql"INSERT INTO student (name, score) VALUES (${s2.name}, ${s2.score})".update
      } yield Seq(i, j)
      insertUsers.transact(conn) should be (Right(Seq(1, 1)))
      insertUsers.transactUnsafe(conn) should be (Seq(1, 1))
    }

    it("executeBatch") {
      val s1 = Student(1, "zdx", 80)
      val s2 = Student(2, "ygy", 100)
      def insertStudent(s: List[Student]): ConnectionIO[Seq[Int]] = {
        val sql = s"INSERT INTO student (id, name, score) VALUES (?, ?, ?)"
        Update[Student](sql).updateMany(s)
      }
      insertStudent(List(s1, s2)).transactUnsafe(conn) should be (Seq(1, 1))
    }
  }

}

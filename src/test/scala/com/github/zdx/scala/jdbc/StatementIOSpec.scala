package com.github.zdx.scala.jdbc

import org.scalatest.{FunSpec, Matchers}

/**
  * Created by zhoudunxiong on 2018/4/21.
  */
class StatementIOSpec extends FunSpec with Matchers {

  import StatementIO._

  describe("Test StatementIO") {

    it("should return Statement") {
      val id = 1
      val expected = StatementIO("select name from student where id=?", id)
      sql"select name from student where id=$id" should be (expected)
    }
  }
}

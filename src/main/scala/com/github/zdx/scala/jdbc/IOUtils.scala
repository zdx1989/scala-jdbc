package com.github.zdx.scala.jdbc

/**
  * Created by zhoudunxiong on 2018/4/21.
  */
object IOUtils {

  def using[A, B <: {def close(): Unit}](b: B)(f: B => A): A =
    try f(b)
    finally b.close()
}

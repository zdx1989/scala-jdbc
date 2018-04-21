# scala-jdbc

scala-jbdc使用Scala语言对jdbc的API做一层封装，提供更易用的API

## Select

查询多条记录
```scala
case class Student(id: Long, name: String, score: Int)

val students: Either[Exception, List[Student]] =
  sql"SELECT * FROM student".query[Student].list.run(conn)
```

查询单条记录
```scala
val student: Either[Exception, Option[Student]] =
  sql"SELECT * FROM student".query[Student].option.run(conn)

val tuple: Either[Exception, (Long, String, Int)] =
  sql"SELECT * FROM student".query[(Long, String, Int)].unique.run(conn)
```

## 更新记录

```scala
val s1 = Student(1, "ygy", 100)
sql"INSERT INTO student (name, score) VALUES (${s1.name}, ${s1.score})".update.run(conn)
```

## 事务

```scala
val s1 = Student(1, "zdx", 80)
val s2 = Student(2, "ygy", 100)
val insertUsers: ConnectionIO[Seq[Int]] = for {
  i <- sql"INSERT INTO student (name, score) VALUES (${s1.name}, ${s1.score})".update
  j <- sql"INSERT INTO student (name, score) VALUES (${s2.name}, ${s2.score})".update
} yield Seq(i, j)
insertUsers.transact(conn)
```

## 特别鸣谢
[doobie](https://github.com/tpolecat/doobie)
[scala-jdbc](https://github.com/takezoe/scala-jdbc)

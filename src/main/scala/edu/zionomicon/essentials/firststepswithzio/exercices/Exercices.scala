package edu.zionomicon
package edu.zionomicon.essentials.firststepswithzio.exercices

object Exercices extends zio.ZIOAppDefault {
  object Exercice1 {

    import zio.ZIO

    def readFile(file: String): String = {
      val source = scala.io.Source.fromFile(file)
      try source.getLines().mkString
      finally source.close()
    }

    def readFileZio(file: String) = ZIO.attempt(readFile(file))
  }

  object Exercice2 {

    import zio.ZIO

    def writeFile(file: String, text: String): Unit = {
      import java.io._
      val pw = new PrintWriter(new File(file))
      try pw.write(text)
      finally pw.close
    }

    def writeFileZio(file: String, text: String) = ZIO.attempt(writeFile(file, text))
  }

  object Exercice3 {

    import Exercice1._
    import Exercice2._

    def copyFile(source: String, dest: String): Unit = {
      val contents = readFile(source)
      writeFile(dest, contents)
    }

    def copyFileZio(source: String, dest: String) =
      readFileZio(source)
        .flatMap(content => writeFileZio(dest, content))
  }

  object Exercice4 {

    import zio.ZIO

    def printLine(line: String) = ZIO.attempt(println(line))

    val readLine = ZIO.attempt(scala.io.StdIn.readLine())

    /*
    printLine("What is your name?").flatMap(_ =>
      readLine.flatMap(name => printLine(s"Hello, $name!"))
    )*/

    for {
      _ <- printLine("What is your name?")
      name <- readLine
      _ <- printLine(s"Hello, $name!")
    } yield ()

  }

  object Exercice5 {

    import zio.ZIO

    val random = ZIO.attempt(scala.util.Random.nextInt(3) + 1)

    def printLine(line: String) = ZIO.attempt(println(line))

    val readLine = ZIO.attempt(scala.io.StdIn.readLine())

    /*random.flatMap { int =>
      printLine("Guess a number from 1 to 3:").flatMap { _ =>
        readLine.flatMap { num =>
          if (num == int.toString) printLine("You guessed right!")
          else printLine(s"You guessed wrong, the number was $int!")
        }}}*/
    for {
      int <- random
      _ <- printLine("Guess a number from 1 to 3:")
      num <- readLine
      _ <- if (num == int.toString) printLine("You guessed right!")
      else printLine(s"You guessed wrong, the number was $int!")
    } yield ()
  }

  object Exercice6 {

    final case class ZIO[-R, +E, +A](run: R => Either[E, A])

    def zipWith[R, E, A, B, C](
                                self: ZIO[R, E, A],
                                that: ZIO[R, E, B]
                              )(f: (A, B) => C): ZIO[R, E, C] =
      ZIO(r => {
        //self.run(r).flatMap(a => that.run(r).map(b => f(a, b)))
        for {
          a <- self.run(r)
          b <- that.run(r)
        } yield f(a, b)
      })
  }

  object Exercice7 {

    import Exercice6._

    /**
     * Collects in a list the results of successful ZIO effect (ignoring errors)
     */
    def collectAll[R, E, A](
                             in: Iterable[ZIO[R, E, A]]
                           ): ZIO[R, E, List[A]] =
      ZIO(r => {
        Right(in.foldRight(List.empty[A])((zio, list) => zio.run(r).fold(_ => list, a => a :: list)))
      })
  }

  object Exercice8 {

    import Exercice6._

    /**
     * Collects in a list the results of successful ZIO effects (ignoring errors)
     */
    def foreach[R, E, A, B](in: Iterable[A])(f: A => ZIO[R, E, B]): ZIO[R, E, List[B]] =
      ZIO(r => {
        Right(in.foldRight(List.empty[B])((a, list) => f(a).run(r).fold(_ => list, b => b :: list)))
      })
  }

  object Exercice9 {

    import Exercice6._

    /**
     * The function should return an effect that tries the left-hand side, but if that effect fails,
     * it will fall back to the effect on the right-hand side.
     */
    def orElse[R, E1, E2, A](self: ZIO[R, E1, A], that: ZIO[R, E2, A]): ZIO[R, E2, A] =
      ZIO(r => {
        self.run(r) match {
          case Right(r) => Right(r)
          case Left(_) => that.run(r)
        }
      })
  }

  object Exercice10 {

    import zio._
    import Exercice1._

    object Cat extends ZIOAppDefault {
      def run = {
        //getArgs.flatMap(files => ZIO.foreach(files)(readFileZio).map(println))
        for {
          args <- getArgs
          _ <- ZIO.foreach(args)(file => readFileZio(file).map(println))
        } yield ()
      }
    }
  }

  object Exercice11 {

    import zio._

    def eitherToZIO[E, A](either: Either[E, A]): ZIO[Any, E, A]
    = either match {
      case Right(r) => ZIO.succeed(r)
      case Left(err) => ZIO.fail(err)
    }
  }

  object Exercice12 {

    import zio._

    def listToZIO[A](list: List[A]): ZIO[Any, None.type, A] =
      if (list.isEmpty) ZIO.fail(None) else ZIO.succeed(list.head)
  }

  object Exercice13 {

    import zio._

    def currentTime(): Long = java.lang.System.currentTimeMillis()

    lazy val currentTimeZIO: ZIO[Any, Nothing, Long] = ZIO.succeed(currentTime())
  }

  object Exercice14 {

    import zio._

    def getCacheValue(
                       key: String,
                       onSuccess: String => Unit,
                       onFailure: Throwable => Unit): Unit = ???

    def getCacheValueZio(key: String): ZIO[Any, Throwable,
      String] = ZIO.async(callback => {
      getCacheValue(key, value => callback(ZIO.succeed(value)), err => callback(ZIO.fail(err)))
    })
  }

  object Exercice15 {

    import zio._

    trait User

    def saveUserRecord(
                        user: User,
                        onSuccess: () => Unit,
                        onFailure: Throwable => Unit
                      ): Unit = ???

    def saveUserRecordZio(user: User): ZIO[Any, Throwable, Unit]
    = ZIO.async(callback => saveUserRecord(user, () => callback(ZIO.succeed(())), err => callback(ZIO.fail(err))))
  }

  object Exercice16 {

    import zio._
    import scala.concurrent.{ExecutionContext, Future}

    trait Query

    trait Result

    def doQuery(query: Query)(implicit ec: ExecutionContext
    ): Future[Result] = ???

    def doQueryZio(query: Query): ZIO[Any, Throwable, Result] =
      ZIO.fromFuture(implicit ec => doQuery(query))
  }

  object Exercice17 {

  }

  object Exercice18 {

  }

  object Exercice19 {

  }

  object Exercice20 {

  }


  override def run: zio.ZIO[Any with zio.ZIOAppArgs with zio.Scope, Any, Any] = zio.ZIO.attempt({
    { //Using zipWith
      import Exercice6._
      zipWith[Int, String, Int, Int, Int](ZIO(_ => Right(1)), ZIO(_ => Right(3)))((a, b) => a + b).run(0).fold(println, println)
    }
    { //Using collectAll
      import Exercice6._
      import Exercice7._
      collectAll[Int, String, Int](List(ZIO(_ => Right(1)), ZIO(_ => Left("Error")), ZIO(_ => Right(2)))).run(0).fold(println, println)
    }
    { //Using foreach
      import Exercice6._
      import Exercice8._
      foreach[Int, String, Int, Double](1 to 10)(i => ZIO(r => if (i == 4) Left("Error") else Right(i / 2.0))).run(0).fold(println, println)
    }
    { //Using orElse
      import Exercice6._
      import Exercice9._
      orElse[Int, String, Int, Double](ZIO(r => Right(1)), ZIO(r => Right(2))).run(0).fold(println, println)
      orElse[Int, String, Int, Double](ZIO(r => Left("Error")), ZIO(r => Right(2))).run(0).fold(println, println)
    }
  })

}

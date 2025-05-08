package edu.zionomicon
package edu.zionomicon.essentials.firststepswithzio.exercices

import zio.ZIO

object Exercices {
  object Exercice1 {
    def readFile(file: String): String = {
      val source = scala.io.Source.fromFile(file)
      try source.getLines().mkString
      finally source.close()
    }

    def readFileZio(file: String) = ZIO.attempt(readFile(file))
  }

  object Exercice2 {

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

  }

  object Exercice7 {

  }

  object Exercice8 {

  }

  object Exercice9 {

  }

  object Exercice10 {

  }

  object Exercice11 {

  }

  object Exercice12 {

  }

  object Exercice13 {

  }

  object Exercice14 {

  }

  object Exercice15 {

  }

  object Exercice16 {

  }

  object Exercice17 {

  }

  object Exercice18 {

  }

  object Exercice19 {

  }

  object Exercice20 {

  }

}

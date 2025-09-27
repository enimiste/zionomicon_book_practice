package edu.zionomicon.ch01_firststepswithzio.chapter

import zio.{Console, Scope, ZIO, ZIOAppArgs, ZIOAppDefault, durationInt}

import java.util.concurrent.{Executors, TimeUnit}
import scala.io.StdIn
import scala.util.Random
import java.io.IOException

//******************************************* subchapter 1.2 :
object App_1_1_001 {
  private def goShoppingUnsafe(): Unit =
    println("Going to Grocery store")

  def main(args: Array[String]): Unit = {
    val executor = Executors.newScheduledThreadPool(2)
    executor.schedule(new Runnable {
      override def run(): Unit = goShoppingUnsafe()
    }, 10, TimeUnit.SECONDS)

    executor.shutdown()
  }
}

object App_1_1_002 extends ZIOAppDefault {
  private val goShopping = ZIO.attempt(println("Going to Grocery Store"))

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = goShopping.delay(10.seconds)

}

//******************************************* subchapter 1.2 :
//flatMap, for-comprehension
object App_1_2_001 extends ZIOAppDefault {
  private val prompt = ZIO.attempt(print("Type something :"))

  private val readLine = ZIO.attempt(StdIn.readLine())

  private def printLine(line: String) = ZIO.attempt(println(line))

  //first syntax
  private val echo1 = prompt.flatMap(_ => readLine.flatMap(line => printLine(line)))

  //second syntax
  private val echo2 = for {
    _ <- prompt
    line <- readLine
    _ <- printLine(line)
  } yield ()

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = echo2.repeatN(2)
}

//******************************************* subchapter 1.3 :
//zipWith, zip
object App_1_3_001 extends ZIOAppDefault {
  private val firstName = ZIO.attempt(StdIn.readLine("What is your first name ?"))
  private val lastName = ZIO.attempt(StdIn.readLine("What is your last name ?"))

  //first syntax
  private val fullName_v1 = for {
    firstname <- firstName
    lastname <- lastName
  } yield println(s"$firstname $lastname")

  //second syntax
  private val fullName_v2 = firstName.zipWith(lastName)((firstname, lastname) => println(s"$firstname $lastname"))

  //third syntax
  private val fullName_v3 = firstName.zip(lastName).map((firstname, lastname) => println(s"$firstname $lastname"))

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = fullName_v3
}

//zipLeft
object App_1_3_002 extends ZIOAppDefault {
  private val hello = ZIO.attempt(print("Hello, "))
  private val world = ZIO.attempt(println("world !"))
  private val helloWorld = hello *> world

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = helloWorld
}

//foreach
object App_1_3_003 extends ZIOAppDefault {
  private def printLine(line: String) = ZIO.attempt(println(line))

  private val printNumbers = ZIO.foreach(1 to 10) { i =>
    printLine(i.toString)
      //To print something after executing this ZIO
      .debug(s"${Thread.currentThread().getName}")
  }

  //No order is granted
  private val printNumbersPar = ZIO.foreachPar(1 to 10) { i =>
    printLine(i.toString)
      //To print something after executing this ZIO
      .debug(s"${Thread.currentThread().getName}")
  }

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = printNumbers
}

//collectAll
object App_1_3_004 extends ZIOAppDefault {
  private def printLine(line: String) = ZIO.attempt(println(line))

  private def random(seed: Int) = ZIO.attempt(new Random(seed).nextInt(100_000))

  private val prints = List(
    printLine("First"),
    printLine("Second"),
    printLine("Fourth"),
    printLine("Fifth")
  )

  private val collectedPrints = ZIO.collectAll(prints).flatMap(xs => printLine(xs.toString))

  private val randoms = List(
    random(1),
    random(2),
    random(3),
    random(1),
    random(10),
  )

  private val collectedRandoms = ZIO.collectAll(randoms).flatMap(xs => printLine(xs.toString))

  private val randomsForeach = ZIO.foreach(1 to 10) { seed =>
    random(seed)
  }

  private val collectedRandomsForeach = for { // same as : randomsForeach.flatMap(xs => printLine(xs.toString))
    xs <- randomsForeach
    _ <- printLine(xs.toString)
  } yield ()

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = collectedPrints *> collectedRandoms *> collectedRandomsForeach
}

//******************************************* subchapter 1.4 :
object App_1_4 {
  //Our simplified ZIO version
  private[App_1_4] final case class ZIO[-R, +E, +A](run: R => Either[E, A]) {
    self =>
    def map[B](ab: A => B): ZIO[R, E, B] = ZIO(r => self.run(r) match {
      case Left(e) => Left(e)
      case Right(a) => Right(ab(a))
    })

    def flatMap[R1 <: R, E1 >: E, B](azb: A => ZIO[R1, E1, B]): ZIO[R1, E1, B] = {
      //one way of doing
      /*ZIO(r => self.run(r) match {
        case Left(e) => Left(e)
        case Right(a) => azb(a).run(r)
      })*/
      //another way
      ZIO(r => self.run(r).fold(e => Left(e), a => azb(a).run(r)))
    }

    def foldZIO[R1 <: R, E1, B](failure: E => ZIO[R1, E1, B], success: A => ZIO[R1, E1, B]): ZIO[R1, E1, B] =
      ZIO(r => {
        self.run(r).fold(e => failure(e), a => success(a)).run(r)
      })

    def fold[B](failure: E => B, success: A => B): ZIO[R, E, B] =
      ZIO(r => {
        Right(
          self.run(r) match {
            case Left(e) => failure(e)
            case Right(a) => success(a)
          })
      })

    def provide(r: R): ZIO[Any, E, A] = ZIO(_ => self.run(r))

    def runUnsafe(r: R): Either[E, A] = run(r)
  }

  private[App_1_4] object ZIO:
    def attempt[R, A](a: => A): ZIO[R, Throwable, A] =
      ZIO(_ => {
        try Right(a)
        catch {
          case (e: Throwable) => Left(e)
        }
      })

    def fail[E](e: => E): ZIO[Any, E, Nothing] = ZIO(_ => Left(e))

    def environment[R]: ZIO[R, Nothing, R] = ZIO(r => Right(r))

  end ZIO

  object APP_1_4_001 {
    private val readInt: ZIO[Any, NumberFormatException, Int] = ???

    private val readAndSumTwoInts: ZIO[Any, NumberFormatException, Int] = {
      for {
        a <- readInt
        b <- readInt
      } yield a + b
    }

    private val program1: ZIO[Any, Throwable, Unit] = readAndSumTwoInts.foldZIO(e => ZIO.attempt(println(e.toString)), v => ZIO.attempt(println(v)))
    private val program2: ZIO[Any, Throwable, Int] = readAndSumTwoInts.fold(e => 0, identity)
  }
}

//******************************************* subchapter 1.5 :



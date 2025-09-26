package edu.zionomicon.ch01_firststepswithzio.chapter

import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, durationInt}

import java.util.concurrent.{Executors, TimeUnit}
import scala.collection.immutable.AbstractSeq
import scala.io.StdIn
import scala.util.Random

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

  //first syntaxe
  private val echo1 = prompt.flatMap(_ => readLine.flatMap(line => printLine(line)))

  //second syntaxe
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

  //first syntaxe
  private val fullName_v1 = for {
    firstname <- firstName
    lastname <- lastName
  } yield println(s"$firstname $lastname")

  //second syntaxe
  private val fullName_v2 = firstName.zipWith(lastName)((firstname, lastname) => println(s"$firstname $lastname"))

  //third syntaxe
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
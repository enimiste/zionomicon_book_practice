package edu.zionomicon.ch01_firststepswithzio.chapter

import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, durationInt}

import java.util.concurrent.{Executors, TimeUnit}
import scala.io.StdIn


object App_11_001 {
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

object App_11_002 extends ZIOAppDefault {
  private val goShopping = ZIO.attempt(println("Going to Grocery Store"))

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = goShopping.delay(10.seconds)

}

object App_12_001 extends ZIOAppDefault {
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

object App_13_001 extends ZIOAppDefault {
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


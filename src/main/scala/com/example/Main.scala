package com.example

import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import cats.implicits._
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object Main extends IOApp.Simple {

  def handleEvent(e: Int): IO[Unit] = IO.println(e)

  def process(fast: Queue[IO, Int], slow: Queue[IO, Int]): IO[Unit] = for {
    e <- fast.tryTake.flatMap(_ match {
      case e: Some[Int] => IO.pure(e)
      case None         => slow.tryTake
    })
    _ <- e.map(handleEvent).getOrElse(IO.unit.delayBy(5 second))
  } yield ()

  def run: IO[Unit] = for {
    fast <- Queue.bounded[IO, Int](5)
    slow <- Queue.bounded[IO, Int](5)
    _ <- List(1, 2, 3, 4, 5).traverse(fast.offer)
    _ <- List(6, 7, 8, 9, 10).traverse(slow.offer)
    _ <- process(fast, slow).foreverM
  } yield ()
}

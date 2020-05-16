package io.typechecked.unity.update4

// Update 3 has full access to fundamentals, update1 and update 2
import io.typechecked.unity.fundamentals._
import io.typechecked.unity.update1.Concepts.AgeConcept
import io.typechecked.unity.update1.Concepts.NameConcept
import io.typechecked.unity.update2.Concepts.PairConcept

import shapeless.tag
import shapeless.tag.@@

object Implementations {

  case class Age2(value: Int) extends AgeConcept {
    override def toString: String = value.toString
  }

  case class Name2(value: String) extends NameConcept {
    override def toString: String = value
  }

  case class Pair2[A, B](_1: A, _2: B) extends PairConcept[A, B] {
    override def toString: String = s"(${this._1}, ${this._2})"
  }

}

object Functions {

  import Implementations._

  // Constructors for our new types
  implicit val CreateAge2: Int ==>: Age2 = Fn { i: Int => Age2(i) }
  implicit val CreateName2: String ==>: Name2 = Fn { s: String => Name2(s) }

  implicit def BuildPair2[A, AImpl, B, BImpl](
    implicit a: A |--> AImpl,
    b: B |--> BImpl,
  ): AImpl ==>: BImpl ==>: Pair2[AImpl, BImpl] = Fn { a =>
    Fn { b => Pair2(a, b) }
  }

}

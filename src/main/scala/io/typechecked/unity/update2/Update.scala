package io.typechecked.unity.update2

// update2 has full access to update1:
import io.typechecked.unity.update1.Concepts.AgeConcept
import io.typechecked.unity.update1.Concepts.UserIdConcept

object Concepts {

  // New version of the UserConcept: We've decided Name is not necessary
  trait User2[Id <: UserIdConcept, Age <: AgeConcept] extends UserConcept {
    def id: Id
    def age: Age
  }

  // Completely new concept of a Pair - this is a generic class
  trait PairConcept[A, B] {
    def _1: A
    def _2: B
  }

  // Our first implementation of a pair
  case class Pair[A, B](_1: A, _2: B) extends PairConcept[A, B]

}

object Functions {

  // User2#age
  implicit def User2ToAge[Id <: UserIdConcept](
    implicit age: Canonical[AgeConcept]
  ): User2[Id, age.Impl] ==>: age.Impl = Fn(_.age)

  // A way to build a User2
  implicit def User2Constructor[Id <: UserIdConcept, Age <: AgeConcept, Name <: NameConcept](
    implicit idImpl: UserIdConcept |--> Id,
    ageImpl: AgeConcept |--> Age,
    nameImpl: NameConcept |--> Name
  ): Id ==>: Age ==>: User2[Id, Age] = Fn { id0: Id =>
    Fn { age0: Age =>
      new User2[Id, Age] {
        val id = id0
        val age = age0
      }
    }
  }

  // A way to build a generic pair
  implicit def BuildPair[A, AImpl, B, BImpl](
    implicit a: A |--> AImpl,
    b: B |--> BImpl,
  ): AImpl ==>: BImpl ==>: Pair[AImpl, BImpl] = Fn { a =>
    Fn { b => Pair(a, b) }
  }


}

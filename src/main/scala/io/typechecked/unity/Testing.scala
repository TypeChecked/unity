package io.typechecked.unity

import io.typechecked.unity.fundamentals._

import shapeless.tag
import shapeless.tag.@@

object Canonicals {

  implicit val idImpl: Canonical.Aux[UserIdConcept, UserId] = null
  implicit val ageImpl: Canonical.Aux[AgeConcept, Age] = null
  implicit val nameImpl: Canonical.Aux[NameConcept, Name] = null

  implicit def pairImpl[A, B](
    implicit a: Canonical[A],
    b: Canonical[B]
  ): Canonical.Aux[PairConcept[A, B], Pair[a.Impl, b.Impl]] = null

  implicit def userImpl(implicit
    id: Canonical[UserIdConcept],
    age: Canonical[AgeConcept],
    name: Canonical[NameConcept]
  ): Canonical.Aux[UserConcept, User[id.Impl, age.Impl, name.Impl]] = null

  // implicit def user2Impl(implicit
  //   id: Canonical[UserIdConcept],
  //   age: Canonical[AgeConcept],
  // ): Canonical.Aux[UserConcept, User2[id.Impl, age.Impl]] = null

}


object Functions {






}

object FunctionsAbstract {



}

object RunTime {
  import Canonicals._
  import Functions._
  import FunctionsAbstract._

  def main(args: Array[String]): Unit = {
    val user: User[UserId, Age, Name] = new User[UserId, Age, Name] {
      val id = UserId(1)
      val age = Age(16)
      val name = Name("john")
    }

    // val user: User2[UserId, Age] = new User2[UserId, Age] {
    //   val id = UserId(1)
    //   val age = Age(16)
    // }

    val result = UserAgePlusOneIsTooYoung.apply(user)
    println(result)

    val nextFiveAges = NextFiveAgesForUser.apply(user)
    println(nextFiveAges)

    // Doesn't compile with User2 as User2 has no name:
    val pair = AgeAndNamePair.apply(user)
    println(pair)
  }

}


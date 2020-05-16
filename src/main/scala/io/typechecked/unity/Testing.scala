package io.typechecked.unity

import io.typechecked.numerology.ternary.TNat
import io.typechecked.numerology.ternary.TNat._

import shapeless.tag
import shapeless.tag.@@

// Tags

trait Incremented

// End tags


// Concept of user id
trait UserIdConcept
case class UserId(value: Int) extends UserIdConcept

// concept of age
trait AgeConcept
case class Age(value: Int) extends AgeConcept

// concept of name
trait NameConcept
case class Name(value: String) extends NameConcept

// concept of a user
trait UserConcept

trait User[Id <: UserIdConcept, Age <: AgeConcept, Name <: NameConcept] extends UserConcept {
  def id: Id
  def age: Age
  def name: Name
}

trait User2[Id <: UserIdConcept, Age <: AgeConcept] extends UserConcept {
  def id: Id
  def age: Age
}

trait PairConcept[A, B] {
  def _1: A
  def _2: B
}

case class Pair[A, B](_1: A, _2: B) extends PairConcept[A, B]

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

  implicit def UserToAge[Id <: UserIdConcept, Name <: NameConcept](
    implicit age: Canonical[AgeConcept]
  ): Fn[User[Id, age.Impl, Name], age.Impl] = Fn(_.age)

  implicit def UserToName[Id <: UserIdConcept, Age <: AgeConcept](
    implicit name: Canonical[NameConcept]
  ): User[Id, Age, name.Impl] ==>: name.Impl = Fn(_.name)

  implicit def User2ToAge[Id <: UserIdConcept](
    implicit age: Canonical[AgeConcept]
  ): Fn[User2[Id, age.Impl], age.Impl] = Fn(_.age)

  implicit def AgeImplIncrement: Fn[Age, Age @@ Incremented] = Fn { age =>
    tag[Incremented][Age](Age(age.value + 1))
  }

  implicit def AgeImplLt: Fn[Age, Fn[Int, Boolean]] = Fn { age =>
    Fn { limit =>
      age.value < limit
    }
  }

  implicit val CreateAge: Int ==>: Age = Fn { i: Int => Age(i) }
  implicit val CreateUserId: Int ==>: UserId = Fn { i: Int => UserId(i) }
  implicit val CreateName: String ==>: Name = Fn { s: String => Name(s) }

  implicit def UserConstructor[Id <: UserIdConcept, Age <: AgeConcept, Name <: NameConcept](
    implicit idImpl: Canonical.Aux[UserIdConcept, Id],
    ageImpl: Canonical.Aux[AgeConcept, Age],
    nameImpl: Canonical.Aux[NameConcept, Name]
  ): Id ==>: Age ==>: Name ==>: User[Id, Age, Name] = Fn { id0: Id =>
    Fn { age0: Age =>
      Fn { name0: Name =>
        new User[Id, Age, Name] {
          val id = id0
          val age = age0
          val name = name0
        }
      }
    }
  }

  implicit def User2Constructor[Id <: UserIdConcept, Age <: AgeConcept, Name <: NameConcept](
    implicit idImpl: Canonical.Aux[UserIdConcept, Id],
    ageImpl: Canonical.Aux[AgeConcept, Age],
    nameImpl: Canonical.Aux[NameConcept, Name]
  ): Id ==>: Age ==>: User2[Id, Age] = Fn { id0: Id =>
    Fn { age0: Age =>
      new User2[Id, Age] {
        val id = id0
        val age = age0
      }
    }
  }

  implicit def BuildList[T, Impl](
    implicit t: T |--> Impl
  ): Impl ==>: List[Impl] = Fn { t: Impl => List(t) }

  implicit def PrependList[T, Impl](
    implicit t: T |--> Impl
  ): List[Impl] ==>: Impl ==>: List[Impl] = Fn { list: List[Impl] =>
    Fn { t: Impl => t :: list }
  }

  implicit def BuildPair[A, AImpl, B, BImpl](
    implicit a: A |--> AImpl,
    b: B |--> BImpl,
  ): AImpl ==>: BImpl ==>: Pair[AImpl, BImpl] = Fn { a =>
    Fn { b => Pair(a, b) }
  }

  implicit def UserAgeIncremented[User <: UserConcept, Age <: AgeConcept](
    implicit user: Canonical.Aux[UserConcept, User],
    age: Canonical.Aux[AgeConcept, Age],
    toAge: Fn[User, Age],
    incr: Fn[Age, Age @@ Incremented]
  ): Fn[User, Age @@ Incremented] = Fn { user: User => incr(toAge(user)) }

}

object FunctionsAbstract {

  implicit def UserAgePlusOneIsTooYoung[User <: UserConcept, Age <: AgeConcept](
    implicit user: UserConcept |--> User,
    age: Canonical.Aux[AgeConcept, Age],
    userAgePlusOne: Fn[User, Age @@ Incremented],
    ageLt: Age ==>: Int ==>: Boolean
  ): Fn[User, Boolean] = Fn { user: User =>
    val newAge = userAgePlusOne(user)
    ageLt(newAge)(18)
  }

  implicit def NextFiveAgesForUser[User <: UserConcept, Age <: AgeConcept](
    implicit user: UserConcept |--> User,
    age: AgeConcept |--> Age,
    getAge: User ==>: Age,
    incr: Age ==>: (Age @@ Incremented),
    list: Age ==>: List[Age],
    prepend: List[Age] ==>: Age ==>: List[Age]
  ): User ==>: List[Age] = Fn { user =>
    val age1 = getAge(user)
    val age2 = incr(age1)
    val age3 = incr(age2)
    val age4 = incr(age3)
    val age5 = incr(age4)

    val list1 = list(age1)
    val list2 = prepend(list1)(age2)
    val list3 = prepend(list2)(age3)
    val list4 = prepend(list3)(age4)
    val list5 = prepend(list4)(age5)

    list5
  }

  implicit def AgeAndNamePair[User <: UserConcept, Age <: AgeConcept, Name <: NameConcept, Pair[_, _] <: PairConcept[_, _]](
    implicit user: UserConcept |--> User,
    age: AgeConcept |--> Age,
    name: NameConcept |--> Name,
    pair: PairConcept[AgeConcept, NameConcept] |--> Pair[Age, Name],
    getAge: User ==>: Age,
    getName: User ==>: Name,
    buildPair: Age ==>: Name ==>: Pair[Age, Name]
  ): User ==>: Pair[Age, Name] = Fn { user =>
    buildPair(getAge(user))(getName(user))
  }

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


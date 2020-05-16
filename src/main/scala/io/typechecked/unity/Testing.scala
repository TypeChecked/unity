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

object Canonicals {

  implicit val idImpl: Canonical.Aux[UserIdConcept, UserId] = null
  implicit val ageImpl: Canonical.Aux[AgeConcept, Age] = null
  implicit val nameImpl: Canonical.Aux[NameConcept, Name] = null

  // implicit def userImpl(implicit
  //   id: Canonical[UserIdConcept],
  //   age: Canonical[AgeConcept],
  //   name: Canonical[NameConcept]
  // ): Canonical.Aux[UserConcept, User[id.Impl, age.Impl, name.Impl]] = null

  implicit def user2Impl(implicit
    id: Canonical[UserIdConcept],
    age: Canonical[AgeConcept],
  ): Canonical.Aux[UserConcept, User2[id.Impl, age.Impl]] = null

}


object Functions {

  implicit def UserToAge[Id <: UserIdConcept, Name <: NameConcept](
    implicit age: Canonical[AgeConcept]
  ): Fn[User[Id, age.Impl, Name], age.Impl] = Fn(_.age)

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
}

object FunctionsAbstract {
  implicit def UserConceptToAgeConcept[U <: UserConcept, A <: AgeConcept](
    implicit user: UserConcept |--> U,
    age: Canonical.Aux[AgeConcept, A],
    fn: Fn[U, A]
  ): UserConcept ==>: AgeConcept = Fn { u: UserConcept => fn(u.asInstanceOf[U]) }  // we know all UserConcepts are U

  implicit def IncrementAge[Age <: AgeConcept](
    implicit age: Canonical.Aux[AgeConcept, Age],
    fn: Fn[Age, Age @@ Incremented],
  ): Fn[AgeConcept, AgeConcept @@ Incremented] = Fn { age => fn(age.asInstanceOf[Age]) }

  implicit def UserAgeIncremented[User <: UserConcept, Age <: AgeConcept](
    implicit user: Canonical.Aux[UserConcept, User],
    age: Canonical.Aux[AgeConcept, Age],
    toAge: Fn[User, Age],
    incr: Fn[Age, Age @@ Incremented]
  ): Fn[User, Age @@ Incremented] = Fn { user: User => incr(toAge(user)) }

  implicit def AgeLt[Age <: AgeConcept](
    implicit age: Canonical.Aux[AgeConcept, Age],
    fn: Fn[Age, Fn[Int, Boolean]]
  ): Fn[AgeConcept, Fn[Int, Boolean]] = Fn { age =>
    Fn { limit =>
      fn(age.asInstanceOf[Age])(limit)
    }
  }

  implicit def UserAgePlusOneIsTooYoung[User <: UserConcept, Age <: AgeConcept](
    implicit user: Canonical.Aux[UserConcept, User],
    age: Canonical.Aux[AgeConcept, Age],
    userAgePlusOne: Fn[User, Age @@ Incremented],
    ageLt: Fn[Age, Fn[Int, Boolean]]
  ): Fn[User, Boolean] = Fn { user: User =>
    val newAge = userAgePlusOne(user)
    ageLt(newAge)(18)
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

    val user2: User2[UserId, Age] = new User2[UserId, Age] {
      val id = UserId(1)
      val age = Age(16)
    }

    val result = UserAgePlusOneIsTooYoung.apply(user2)
    println(result)
  }

}


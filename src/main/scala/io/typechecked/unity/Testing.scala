package io.typechecked
package unity

import io.typechecked.numerology.ternary.TNat
import io.typechecked.numerology.ternary.TNat._

import shapeless.tag
import shapeless.tag.@@

// Tags

trait Incremented

// End tags



trait Fn[A, B] { def apply(a: A): B }
object Fn {
  def apply[A, B](fn: A => B): Fn[A, B] = new Fn[A, B] { def apply(a: A): B = fn(a) }
}

trait Implementation[Concept] {
  type Impl <: Concept
}

object Implementation {
  type Aux[Concept, Impl0] = Implementation[Concept] { type Impl = Impl0 }
}

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

object Implementations {

  implicit val idImpl: Implementation.Aux[UserIdConcept, UserId] = null
  implicit val ageImpl: Implementation.Aux[AgeConcept, Age] = null
  implicit val nameImpl: Implementation.Aux[NameConcept, Name] = null

  // implicit def userImpl(implicit
  //   id: Implementation[UserIdConcept],
  //   age: Implementation[AgeConcept],
  //   name: Implementation[NameConcept]
  // ): Implementation.Aux[UserConcept, User[id.Impl, age.Impl, name.Impl]] = null

  implicit def user2Impl(implicit
    id: Implementation[UserIdConcept],
    age: Implementation[AgeConcept],
  ): Implementation.Aux[UserConcept, User2[id.Impl, age.Impl]] = null

}


object Functions {

  implicit def UserToAge[Id <: UserIdConcept, Name <: NameConcept](
    implicit age: Implementation[AgeConcept]
  ): Fn[User[Id, age.Impl, Name], age.Impl] = Fn(_.age)

  implicit def User2ToAge[Id <: UserIdConcept](
    implicit age: Implementation[AgeConcept]
  ): Fn[User2[Id, age.Impl], age.Impl] = Fn(_.age)

  implicit def AgeImplIncrement: Fn[Age, Age @@ Incremented] = Fn { age =>
    tag[Incremented][Age](Age(age.value + 1))
  }

  implicit def AgeImplLt: Fn[Age, Fn[Int, Boolean]] = Fn { age =>
    Fn { limit =>
      age.value < limit
    }
  }

  implicit def produceList[T]: Fn[T, List[T]] = Fn { t: T => List(t) }

}

object FunctionsAbstract {
  implicit def UserConceptToAgeConcept[U <: UserConcept, A <: AgeConcept](
    implicit user: Implementation.Aux[UserConcept, U],
    age: Implementation.Aux[AgeConcept, A],
    fn: Fn[U, A]
  ): Fn[UserConcept, AgeConcept] = Fn { u: UserConcept => fn(u.asInstanceOf[U]) }  // we know all UserConcepts are U


  implicit def IncrementAge[Age <: AgeConcept](
    implicit age: Implementation.Aux[AgeConcept, Age],
    fn: Fn[Age, Age @@ Incremented],
  ): Fn[AgeConcept, AgeConcept @@ Incremented] = Fn { age => fn(age.asInstanceOf[Age]) }

  implicit def UserAgeIncremented[User <: UserConcept, Age <: AgeConcept](
    implicit user: Implementation.Aux[UserConcept, User],
    age: Implementation.Aux[AgeConcept, Age],
    toAge: Fn[User, Age],
    incr: Fn[Age, Age @@ Incremented]
  ): Fn[User, Age @@ Incremented] = Fn { user: User => incr(toAge(user)) }

  implicit def AgeLt[Age <: AgeConcept](
    implicit age: Implementation.Aux[AgeConcept, Age],
    fn: Fn[Age, Fn[Int, Boolean]]
  ): Fn[AgeConcept, Fn[Int, Boolean]] = Fn { age =>
    Fn { limit =>
      fn(age.asInstanceOf[Age])(limit)
    }
  }

  implicit def UserAgePlusOneIsTooYoung[User <: UserConcept, Age <: AgeConcept](
    implicit user: Implementation.Aux[UserConcept, User],
    age: Implementation.Aux[AgeConcept, Age],
    userAgePlusOne: Fn[User, Age @@ Incremented],
    ageLt: Fn[Age, Fn[Int, Boolean]]
  ): Fn[User, Boolean] = Fn { user: User =>
    val newAge = userAgePlusOne(user)
    ageLt(newAge)(18)
  }

  // implicit def PutUserInList

}

object RunTime {
  import Implementations._
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

    val result: Boolean = UserAgePlusOneIsTooYoung.apply(user2)
    // val result: Boolean = UserAgePlusOneIsTooYoung.apply(user)

    println(result)
  }

}

// trait Versioned[Concept, V <: TNat] {
//   type Out <: Concept
//   def impl: Out
// }

// object Versioned {
//   type Aux[Concept, V <: TNat, Out0 <: Concept] = Versioned[Concept, V] { type Out = Out0 }

//   def apply[Concept, V <: TNat, Out0 <: Concept](out: Out0): Versioned.Aux[Concept, V, Out0] = new Versioned[Concept, V] {
//     type Out = Out0
//     val impl = out
//   }
// }

// trait Canonical[Concept] {
//   type Out <: Concept
//   def impl: Out
// }

// object Canonical {
//   type Aux[Concept, Out0 <: Concept] = Canonical[Concept] { type Out = Out0 }

//   def apply[Concept, Out0 <: Concept](impl0: Out0): Canonical.Aux[Concept, Out0] = new Canonical[Concept] {
//     type Out = Out0
//     val impl = impl0
//   }
// }

// // Domain
// case class UserId(value: Int) extends AnyVal
// case class User(id: UserId, name: String)

// // Concepts
// // You write your own ones in the latest library, and reference previously-defined ones
// trait GetUser { def apply(userId: UserId): User }
// trait FindUserByInt { def apply(i: Int): Option[User] }


// // Implementations
// // You write any you like and call them whatever you like

// trait GetUser1 extends GetUser {
//   def apply(userId: UserId): User = User(userId, "johnny")
// }

// trait GetUser2 extends GetUser {
//   def apply(userId: UserId): User = User(userId, "jeremy")
// }

// class FindUserByInt1(implicit getUser: Canonical[GetUser]) extends FindUserByInt {
//   def apply(i: Int): Option[User] = Some(getUser.impl(UserId(i)))
// }

// // Registrations
// // Register all your implementations in some central place. These must be unique, across
// // all libraries, otherwise orchestration will fail

// object GetUserRegistration {
//   implicit val versionedGetUser1: Versioned.Aux[GetUser, t1, GetUser1] = Versioned[GetUser, t1, GetUser1](new GetUser1 {})
//   implicit val versionedGetUser2: Versioned.Aux[GetUser, t2, GetUser2] = Versioned[GetUser, t2, GetUser2](new GetUser2 {})
// }

// object FindUerByIntRegistration {
//   implicit def versionedFindUserById1(implicit getUser: Canonical[GetUser]): Versioned.Aux[FindUserByInt, t1, FindUserByInt1] =
//     Versioned[FindUserByInt, t1, FindUserByInt1](new FindUserByInt1 {})
// }

// // Orchestration
// // This is the main app
// // We register all canonical definitions and their versions.
// object OrchestrationLevel {

//   implicit val canonicalGetUser: Canonical.Aux[GetUser, GetUser2] =
//     Canonical[GetUser, GetUser2](new GetUser2 {})

//   implicit val canonicalFindUserByInt: Canonical.Aux[FindUserByInt, FindUserByInt1] =
//     Canonical[FindUserByInt, FindUserByInt1](new FindUserByInt1())

// }

// object RunTime {
//   def main(args: Array[String]): Unit = {
//     import OrchestrationLevel._
//     val function = implicitly[Canonical[FindUserByInt]]
//     println(function.impl(19))
//   }
// }

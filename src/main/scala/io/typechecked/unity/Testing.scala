package io.typechecked
package unity

import io.typechecked.numerology.ternary.TNat
import io.typechecked.numerology.ternary.TNat._

trait Versioned[Concept, V <: TNat] {
  type Out <: Concept
  def impl: Out
}

object Versioned {
  type Aux[Concept, V <: TNat, Out0 <: Concept] = Versioned[Concept, V] { type Out = Out0 }

  def apply[Concept, V <: TNat, Out0 <: Concept](out: Out0): Versioned.Aux[Concept, V, Out0] = new Versioned[Concept, V] {
    type Out = Out0
    val impl = out
  }
}

trait Canonical[Concept] {
  type Out <: Concept
  def impl: Out
}

object Canonical {
  type Aux[Concept, Out0 <: Concept] = Canonical[Concept] { type Out = Out0 }

  def apply[Concept, Out0 <: Concept](impl0: Out0): Canonical.Aux[Concept, Out0] = new Canonical[Concept] {
    type Out = Out0
    val impl = impl0
  }
}

// Domain
case class UserId(value: Int) extends AnyVal
case class User(id: UserId, name: String)

// Concepts
// You write your own ones in the latest library, and reference previously-defined ones
trait GetUser { def apply(userId: UserId): User }
trait FindUserByInt { def apply(i: Int): Option[User] }


// Implementations
// You write any you like and call them whatever you like

trait GetUser1 extends GetUser {
  def apply(userId: UserId): User = User(userId, "johnny")
}

trait GetUser2 extends GetUser {
  def apply(userId: UserId): User = User(userId, "jeremy")
}

class FindUserByInt1(implicit getUser: Canonical[GetUser]) extends FindUserByInt {
  def apply(i: Int): Option[User] = Some(getUser.impl(UserId(i)))
}

// Registrations
// Register all your implementations in some central place. These must be unique, across
// all libraries, otherwise orchestration will fail

object GetUserRegistration {
  implicit val versionedGetUser1: Versioned.Aux[GetUser, t1, GetUser1] = Versioned[GetUser, t1, GetUser1](new GetUser1 {})
  implicit val versionedGetUser2: Versioned.Aux[GetUser, t2, GetUser2] = Versioned[GetUser, t2, GetUser2](new GetUser2 {})
}

object FindUerByIntRegistration {
  implicit def versionedFindUserById1(implicit getUser: Canonical[GetUser]): Versioned.Aux[FindUserByInt, t1, FindUserByInt1] =
    Versioned[FindUserByInt, t1, FindUserByInt1](new FindUserByInt1 {})
}

// Orchestration
// This is the main app
// We register all canonical definitions and their versions.
object OrchestrationLevel {

  implicit val canonicalGetUser: Canonical.Aux[GetUser, GetUser2] =
    Canonical[GetUser, GetUser2](new GetUser2 {})

  implicit val canonicalFindUserByInt: Canonical.Aux[FindUserByInt, FindUserByInt1] =
    Canonical[FindUserByInt, FindUserByInt1](new FindUserByInt1())

}

object RunTime {
  def main(args: Array[String]): Unit = {
    import OrchestrationLevel._
    val function = implicitly[Canonical[FindUserByInt]]
    println(function.impl(19))
  }
}

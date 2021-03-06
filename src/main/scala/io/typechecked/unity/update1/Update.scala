package io.typechecked.unity.update1

import io.typechecked.unity.fundamentals._

object Concepts {
  trait UserIdConcept  // Concept of user id
  trait AgeConcept  // concept of age
  trait NameConcept  // concept of name
  trait UserConcept  // concept of a user
}

object Implementations {

  import Concepts._

  case class UserId(value: Int) extends UserIdConcept
  case class Age(value: Int) extends AgeConcept
  case class Name(value: String) extends NameConcept

  // Implementation of a user: Does not know what implementations it contains in its fields
  trait User[Id <: UserIdConcept, Age <: AgeConcept, Name <: NameConcept] extends UserConcept {
    def id: Id
    def age: Age
    def name: Name
  }

}

object Functions {

  import Concepts._
  import Implementations._

  // Constructors for basic implementation types
  implicit val CreateAge: Int ==>: Age = Fn { i: Int => Age(i) }
  implicit val CreateUserId: Int ==>: UserId = Fn { i: Int => UserId(i) }
  implicit val CreateName: String ==>: Name = Fn { s: String => Name(s) }

  // User#age accessor
  implicit def UserToAge[Id <: UserIdConcept, Name <: NameConcept](
    implicit age: Canonical[AgeConcept]
  ): Fn[User[Id, age.Impl, Name], age.Impl] = Fn(_.age)

  // User#name accessor
  implicit def UserToName[Id <: UserIdConcept, Age <: AgeConcept](
    implicit name: Canonical[NameConcept]
  ): User[Id, Age, name.Impl] ==>: name.Impl = Fn(_.name)

  // Constructor for complex type based on inner type implementation
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
}

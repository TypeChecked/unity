package io.typechecked.unity.update6

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import io.typechecked.unity.fundamentals._

object Functions {

  import io.typechecked.unity.update1.Concepts._
  import io.typechecked.unity.update1.Implementations._

  implicit def UserIdToString: UserId ==>: String = Fn(_.value.toString)
  implicit def NameToString: Name ==>: String = Fn(_.value)
  implicit def AgeToString: Age ==>: String = Fn(_.value.toString)

  // In reality would be a Json typeclass
  // Here we fix our bug with the update5 version
  // In reality this would be achieved by specifying encoder typeclasses for our field types
  // Here instead we are just going to use Show
  implicit def UserToJsonV2[Id <: UserIdConcept, Age <: AgeConcept, Name <: NameConcept](
    implicit id: UserIdConcept |--> Id,
    age: AgeConcept |--> Age,
    name: NameConcept |--> Name,
    toId: User[Id, Age, Name] ==>: Id,
    toName: User[Id, Age, Name] ==>: Name,
    toAge: User[Id, Age, Name] ==>: Age,
    idToString: Id ==>: String,
    nameToString: Name ==>: String,
    ageToString: Age ==>: String
  ): User[Id, Age, Name] ==>: String = Fn { user => s"""{
    |  "id": ${idToString(toId(user))},
    |  "name": "${nameToString(toName(user))}",
    |  "age": ${ageToString(toAge(user))}
    |}""".stripMargin
  }
}

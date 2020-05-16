package io.typechecked.unity.update5

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import io.typechecked.unity.fundamentals._

object Functions {

  import io.typechecked.unity.update1.Concepts._
  import io.typechecked.unity.update1.Implementations.User
  import io.typechecked.unity.update1.Implementations.UserId

  implicit val IdToInt: UserId ==>: Int = Fn(_.value)

  implicit def UserToId[Name <: NameConcept, Age <: AgeConcept](
    implicit id: Canonical[UserIdConcept]
  ): User[id.Impl, Age, Name] ==>: id.Impl = Fn(_.id)

  // In reality would be a Json typeclass
  implicit def UserToJson[Id <: UserIdConcept, Age <: AgeConcept, Name <: NameConcept](
    implicit id: UserIdConcept |--> Id,
    age: AgeConcept |--> Age,
    name: NameConcept |--> Name,
    toId: User[Id, Age, Name] ==>: Id,
    toName: User[Id, Age, Name] ==>: Name,
    toAge: User[Id, Age, Name] ==>: Age
  ): User[Id, Age, Name] ==>: String = Fn { user => s"""{
    |  "id": ${toId(user)},
    |  "name": "${toName(user)}",
    |  "age": ${toAge(user)}
    |}""".stripMargin
  }

  // Database simulation, getting a User from DB
  implicit def GetUserFromDb[Id <: UserIdConcept, Age <: AgeConcept, Name <: NameConcept](
    implicit id: UserIdConcept |--> Id,
    age: AgeConcept |--> Age,
    name: NameConcept |--> Name,
    idToInt: Id ==>: Int,
    wrapAge: Int ==>: Age,
    wrapName: String ==>: Name,
    buildUser: Id ==>: Age ==>: Name ==>: User[Id, Age, Name]
  ): Id ==>: Future[Option[User[Id, Age, Name]]] = Fn { id =>
    val int = idToInt(id)

    val user = if (int == 1) {
      Some(buildUser(id)(wrapAge(53))(wrapName("jeremy")))
    }
    else if (int == 2) {
      Some(buildUser(id)(wrapAge(19))(wrapName("john")))
    }
    else None

    Future.successful(user)
  }

  implicit def ProjectRoute[Id <: UserIdConcept, User <: UserConcept](
    implicit id: UserIdConcept |--> Id,
    user: UserConcept |--> User,
    idFromInt: Int ==>: Id,
    getUserFromDb: Id ==>: Future[Option[User]],
    userJson: User ==>: String
  ): Unit ==>: Route = Fn { _ =>
    pathPrefix("users") {
      pathPrefix(IntNumber) { idInt =>
        get {
          val id = idFromInt(idInt)
          val user = getUserFromDb(id)
          onSuccess(user) {
            case None => complete(StatusCodes.NotFound)
            case Some(u) => complete(userJson(u))
          }
        }
      }
    }
  }
}

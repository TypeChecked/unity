package io.typechecked.unity.update7

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import io.typechecked.unity.fundamentals._
import io.typechecked.unity.update1.Concepts._
  import io.typechecked.unity.update1.Implementations._

object Implementations {
  // This is our new Name concept implementation
  case class FullName(first: String, last: String) extends NameConcept
}

object Functions {

  import Implementations._

  // We need a few bits of functionality for our new FullName class:

  // How to show the fullname in json
  implicit def FullNameToString: FullName ==>: String = Fn { n =>
    s"${n.first} ${n.last}"
  }

  // How to read the fullname from a pair of strings from the DB
  implicit def WrapName: (String, String) ==>: FullName = Fn(FullName.tupled)

  // Database simulation update: We now store first and last names on User!
  // This is equivalent to updating your slick shape table to access the new column(s)
  implicit def GetUserFromDbV2[Id <: UserIdConcept, Age <: AgeConcept, Name <: NameConcept](
    implicit id: UserIdConcept |--> Id,
    age: AgeConcept |--> Age,
    name: NameConcept |--> Name,
    idToInt: Id ==>: Int,
    wrapAge: Int ==>: Age,
    wrapName: (String, String) ==>: Name,
    buildUser: Id ==>: Age ==>: Name ==>: User[Id, Age, Name]
  ): Id ==>: Future[Option[User[Id, Age, Name]]] = Fn { id =>
    val int = idToInt(id)

    val user = if (int == 1) {
      Some(buildUser(id)(wrapAge(53))(wrapName("jeremy" -> "jackson")))
    }
    else if (int == 2) {
      Some(buildUser(id)(wrapAge(19))(wrapName("john" -> "baptist")))
    }
    else None

    Future.successful(user)
  }
}

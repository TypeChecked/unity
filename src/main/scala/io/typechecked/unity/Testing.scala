package io.typechecked.unity

import io.typechecked.unity.fundamentals._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

// This is what "main" code looks like
// We take the setup in update 1, ignore the dummy example code in updates 2, 3 and 4
// and take the project route and implementation from update 5
// And run the route
// This is the only mutable code in the whole project
object App1 {

  import io.typechecked.unity.update1.Concepts._
  import io.typechecked.unity.update1.Functions._
  import io.typechecked.unity.update1.Implementations._
  import io.typechecked.unity.update5.Functions._

  def main(args: Array[String]): Unit = {

    implicit val idImpl: Canonical.Aux[UserIdConcept, UserId] = null
    implicit val ageImpl: Canonical.Aux[AgeConcept, Age] = null
    implicit val nameImpl: Canonical.Aux[NameConcept, Name] = null

    implicit def userImpl(implicit
      id: Canonical[UserIdConcept],
      age: Canonical[AgeConcept],
      name: Canonical[NameConcept]
    ): Canonical.Aux[UserConcept, User[id.Impl, age.Impl, name.Impl]] = null

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route = ProjectRoute.apply(())

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}

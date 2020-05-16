package io.typechecked.unity

import io.typechecked.unity.fundamentals._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

/*
This is what "main" code looks like
We take the setup in update 1, ignore the dummy example code in updates 2, 3 and 4
and take the project route and implementation from update 5
And run the route
This is the only mutable code in the whole project
*/
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

/*
  There's a bug! All the json representations of our fields are wrong
  We need to write a new User ==>: String which corrects this.
  This in our update6.Functions library object.
  We explicitly ignore the buggy json function from update 5.
  The diff between App1 and App2 is:

  - import io.typechecked.unity.update5.Functions._
  + import io.typechecked.unity.update5.Functions.{UserToJson => _, _}
  + import io.typechecked.unity.update6.Functions._

The API now returns:

{
  "id": 1,
  "name": "jeremy",
  "age": 53
}

*/
object App2 {

  import io.typechecked.unity.update1.Concepts._
  import io.typechecked.unity.update1.Functions._
  import io.typechecked.unity.update1.Implementations._
  import io.typechecked.unity.update5.Functions.{UserToJson => _, _}
  import io.typechecked.unity.update6.Functions._

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

/*
We have new functionality that lets us store first and last names, inside Name!

Here's the diff of the main app, after our update:

- implicit val nameImpl: Canonical.Aux[NameConcept, Name] = null
+ implicit val nameImpl: Canonical.Aux[NameConcept, FullName] = null
+ import io.typechecked.unity.update7.Implementations._
+ import io.typechecked.unity.update7.Functions._

Imports for new functionality, and swapping our canonical name implementation, and that's it!

The API now returns:

{
  "id": 1,
  "name": "jeremy jackson",
  "age": 53
}
*/
object App3 {

  import io.typechecked.unity.update1.Concepts._
  import io.typechecked.unity.update1.Functions._
  import io.typechecked.unity.update1.Implementations._
  import io.typechecked.unity.update5.Functions.{UserToJson => _, _}
  import io.typechecked.unity.update6.Functions._
  import io.typechecked.unity.update7.Implementations._
  import io.typechecked.unity.update7.Functions._

  def main(args: Array[String]): Unit = {

    implicit val idImpl: Canonical.Aux[UserIdConcept, UserId] = null
    implicit val ageImpl: Canonical.Aux[AgeConcept, Age] = null

    // We choose our new class, FullName, to satisfy the NameConcept
    implicit val nameImpl: Canonical.Aux[NameConcept, FullName] = null

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

/*
  The CEO complained that our data was of poor quality, so we wrote a nicer toString method for FullName

  We hide the old implementation, and import the new one.

  This is the diff after we've made our change:

  - import io.typechecked.unity.update7.Functions._
  + import io.typechecked.unity.update7.Functions.{FullNameToString => _, _}
  + import io.typechecked.unity.update8.Functions._

The API now returns:

{
  "id": 1,
  "name": "Jeremy Jackson",
  "age": 53
}

Title case name now!

And of course, if our title case names turn out to be buggy, we can revert any time we like
*/
object App4 {

  import io.typechecked.unity.update1.Concepts._
  import io.typechecked.unity.update1.Functions._
  import io.typechecked.unity.update1.Implementations._
  import io.typechecked.unity.update5.Functions.{UserToJson => _, _}
  import io.typechecked.unity.update6.Functions._
  import io.typechecked.unity.update7.Implementations._
  import io.typechecked.unity.update7.Functions.{FullNameToString => _, _}
  import io.typechecked.unity.update8.Functions._

  def main(args: Array[String]): Unit = {

    implicit val idImpl: Canonical.Aux[UserIdConcept, UserId] = null
    implicit val ageImpl: Canonical.Aux[AgeConcept, Age] = null

    // We choose our new class, FullName, to satisfy the NameConcept
    implicit val nameImpl: Canonical.Aux[NameConcept, FullName] = null

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

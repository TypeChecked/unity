package io.typechecked.unity

import io.typechecked.unity.fundamentals._

// This is what "main" code looks like with access to update1 only
object Version1 {

  import io.typechecked.unity.update1.Concepts._
  import io.typechecked.unity.update1.Implementations._
  import io.typechecked.unity.update1.Functions._

  // Choose our canonical definitions
  implicit val idImpl: Canonical.Aux[UserIdConcept, UserId] = null
  implicit val ageImpl: Canonical.Aux[AgeConcept, Age] = null
  implicit val nameImpl: Canonical.Aux[NameConcept, Name] = null

  // Defined but not actually needed, as there are no algorithms over a user implementation yet
  implicit def userImpl(implicit
    id: Canonical[UserIdConcept],
    age: Canonical[AgeConcept],
    name: Canonical[NameConcept]
  ): Canonical.Aux[UserConcept, User[id.Impl, age.Impl, name.Impl]] = null

  // Example of what is possible to describe here:
  val age = CreateAge(19)
  val name = CreateName("johnny")
  val id = CreateUserId(1902)

  val user = UserConstructor.apply(id)(age)(name)
  val nameFromUser = UserToName.apply(user)

  def main(args: Array[String]): Unit = {
    println(nameFromUser)
  }

}

// This is what "main" code can look like with access to update1 + 2
object Version2 {
  import io.typechecked.unity.update1.Concepts._
  import io.typechecked.unity.update1.Implementations._
  import io.typechecked.unity.update1.Functions._
  import io.typechecked.unity.update2.Concepts._
  import io.typechecked.unity.update2.Implementations._
  import io.typechecked.unity.update2.Functions._

  implicit val idImpl: Canonical.Aux[UserIdConcept, UserId] = null
  implicit val ageImpl: Canonical.Aux[AgeConcept, Age] = null
  implicit val nameImpl: Canonical.Aux[NameConcept, Name] = null

  // We've chosen to use a new implementation of user - User2
  implicit def user2Impl(implicit
    id: Canonical[UserIdConcept],
    age: Canonical[AgeConcept],
  ): Canonical.Aux[UserConcept, User2[id.Impl, age.Impl]] = null

}

object Version3 {

  import io.typechecked.unity.update1.Concepts._
  import io.typechecked.unity.update1.Implementations._
  import io.typechecked.unity.update1.Functions._
  import io.typechecked.unity.update2.Concepts._
  import io.typechecked.unity.update2.Implementations._
  import io.typechecked.unity.update2.Functions._
  import io.typechecked.unity.update3.Concepts._
  import io.typechecked.unity.update3.Implementations._
  import io.typechecked.unity.update3.Functions._

  implicit val idImpl: Canonical.Aux[UserIdConcept, UserId] = null
  implicit val ageImpl: Canonical.Aux[AgeConcept, Age] = null
  implicit val nameImpl: Canonical.Aux[NameConcept, Name] = null

  // We decide that User2 was a mistake and revert to the original User
  implicit def userImpl(implicit
    id: Canonical[UserIdConcept],
    age: Canonical[AgeConcept],
    name: Canonical[NameConcept]
  ): Canonical.Aux[UserConcept, User[id.Impl, age.Impl, name.Impl]] = null

  // We have an algorithm abstract over Pair, so we have to pick a canonical implementation
  implicit def pairImpl[A, B](
    implicit a: Canonical[A],
    b: Canonical[B]
  ): Canonical.Aux[PairConcept[A, B], Pair[a.Impl, b.Impl]] = null

  def main(args: Array[String]): Unit = {
    val age = CreateAge(19)
    val id = CreateUserId(1902)
    val name = CreateName("johnny")
    val user1 = UserConstructor.apply(id)(age)(name)

    // Call a complex algorith on our user:
    val pair = AgeAndNamePair.apply(user1)

    println(pair)
  }

}


object Version4 {

  import io.typechecked.unity.update1.Concepts._
  import io.typechecked.unity.update1.Functions._
  import io.typechecked.unity.update1.Implementations._
  import io.typechecked.unity.update2.Concepts._
  import io.typechecked.unity.update2.Functions._
  import io.typechecked.unity.update2.Implementations._
  import io.typechecked.unity.update3.Functions.AgeAndNamePair
  import io.typechecked.unity.update4.Functions._
  import io.typechecked.unity.update4.Implementations._

  // Update4 introduced new age and name classes with nicer toString methods
  // We choose these new ones below as our canonicals

  implicit val idImpl: Canonical.Aux[UserIdConcept, UserId] = null
  implicit val ageImpl: Canonical.Aux[AgeConcept, Age2] = null
  implicit val nameImpl: Canonical.Aux[NameConcept, Name2] = null

  // User is the same canonical as before! No need to update for the new Age/Name
  implicit def userImpl(implicit
    id: Canonical[UserIdConcept],
    age: Canonical[AgeConcept],
    name: Canonical[NameConcept]
  ): Canonical.Aux[UserConcept, User[id.Impl, age.Impl, name.Impl]] = null

  // PairConcept also has a new version with a better toString, so we pick the new Pair2
  implicit def pairImpl[A, B](
    implicit a: Canonical[A],
    b: Canonical[B]
  ): Canonical.Aux[PairConcept[A, B], Pair2[a.Impl, b.Impl]] = null

  // This is the same main method as before except for the construction. In reality we would not just create
  // data like this, it would be hidden inside other unchanged algorithms
  def main(args: Array[String]): Unit = {
    val age = CreateAge2(19)
    val id = CreateUserId(1902)
    val name = CreateName2("johnny")
    // This line is unchanged: User does not know what implementations it contains
    val user1 = UserConstructor.apply(id)(age)(name)

    // This line is unchanged: The new age, name and pair are picked up automatically:
    val pair = AgeAndNamePair.apply(user1)

    // The same program now has output: "(19, johnny)" rather than "Pair(Age(19),Name(johnny))"
    println(pair)
  }
}

package io.typechecked.unity.update3

// Update 3 has full access to fundamentals, update1 and update 2
import io.typechecked.unity.fundamentals._
import io.typechecked.unity.update1.Implementations.Age
import io.typechecked.unity.update1.Concepts._
import io.typechecked.unity.update2.Concepts._

import shapeless.tag
import shapeless.tag.@@

object Concepts {

  // A type tag
  trait Incremented

}

object Implementations

object Functions {

  import Concepts._

  // Increment an age
  implicit def AgeImplIncrement(
    implicit constructor: Int ==>: Age
  ): Age ==>: (Age @@ Incremented) = Fn { age =>
    tag[Incremented][Age](constructor(age.value + 1))
  }

  // Algorithm to return a user's incremented age
  implicit def UserAgeIncremented[User <: UserConcept, Age <: AgeConcept](
    implicit user: UserConcept |--> User,
    age: AgeConcept |--> Age,
    toAge: User ==>: Age,
    incr: Age ==>: (Age @@ Incremented)
  ): User ==>: (Age @@ Incremented) = Fn { user: User => incr(toAge(user)) }

  // Lift to List
  implicit def BuildList[T, Impl](
    implicit t: T |--> Impl
  ): Impl ==>: List[Impl] = Fn { t: Impl => List(t) }

  // List prepend
  implicit def PrependList[T, Impl](
    implicit t: T |--> Impl
  ): List[Impl] ==>: Impl ==>: List[Impl] = Fn { list: List[Impl] =>
    Fn { t: Impl => t :: list }
  }

  // Algorithm to return next five ages a user will be
  implicit def NextFiveAgesForUser[User <: UserConcept, Age <: AgeConcept](
    implicit user: UserConcept |--> User,
    age: AgeConcept |--> Age,
    getAge: User ==>: Age,
    incr: Age ==>: (Age @@ Incremented),
    list: Age ==>: List[Age],
    prepend: List[Age] ==>: Age ==>: List[Age]
  ): User ==>: List[Age] = Fn { user =>
    val age1 = getAge(user)
    val age2 = incr(age1)
    val age3 = incr(age2)
    val age4 = incr(age3)
    val age5 = incr(age4)

    val list1 = list(age1)
    val list2 = prepend(list1)(age2)
    val list3 = prepend(list2)(age3)
    val list4 = prepend(list3)(age4)
    val list5 = prepend(list4)(age5)

    list5
  }

  // Algorithm to return the age and name from a user in a pair
  implicit def AgeAndNamePair[User <: UserConcept, Age <: AgeConcept, Name <: NameConcept, Pair[_, _] <: PairConcept[_, _]](
    implicit user: UserConcept |--> User,
    age: AgeConcept |--> Age,
    name: NameConcept |--> Name,
    pair: PairConcept[AgeConcept, NameConcept] |--> Pair[Age, Name],
    getAge: User ==>: Age,
    getName: User ==>: Name,
    buildPair: Age ==>: Name ==>: Pair[Age, Name]
  ): User ==>: Pair[Age, Name] = Fn { user =>
    buildPair(getAge(user))(getName(user))
  }


}

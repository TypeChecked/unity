# unity
Unison for Scala

An experiment to see if it is possible to replicate the immutability of code from the language Unison, in scala.

The 'fundamentals' package contains the only language constructs we need: A typeclass function wrapper (`Fn`), and
an implementation typeclass (`Canonical`).

And a couple of symbols to make writing code a little nicer.

The rest of the code is an example of how a unison-like development flow would happen in scala.

Each `update` package is the result of developer work. These happen offline, independently, in a separate repository.
Each update has access to all update packages before it - in a real workflow these would be arranged as libraries.

Your new update library subscribes to all currently available update libraries. When you are done writing your new code,
you publish your library. In the app they're keyed by integers - in reality they would be keyed by git hash or timestamp.

When you've published your library, you mutate the main repository. This is the only mutable code in the entire project.

The main repository (`Testing.scala`) should be a small object, that starts your service and subscribes to implementations
found in the libraries you have added in a separate stream of work.

The main repository has access to all the libraries written by all developers on all branches of work. It can choose
any implementation it likes for class concepts and implicit function instances.

After you have changed the main repository to use your new code (by erasing the old import and adding your new one, or changing a Canonical implementation),
the compiler will verify that the entire stack of implicit code structure is compatible. If it is, it will build
the entirely new implementation for you automatically. Then we're good! On to the next piece of work.

If it's not compatible, the compilation will fail. At this stage, it's back to write a new update to fill the gap you left
in your implementations. 

This is conflict-free coding, abstracted over source control. At any point, the main program has access to all previous
code ever defined. New code is made with incremental additions, no bugs can ever be introduced in old code. Never again will you
have to compile 1,000s of source files every time you make a change, you will only ever have to compile new code, and the 
final verification.

Source control and incremental compilation, as code.

## Code Explanation

### Types and classes

Every single class and type is subject to change. There is no common interface between any types of the same thread, other than a simple trait token. For example, `Name` and `FullName` are both `NameConcept`s. At orchestration time, in the main app, we must choose which of the two `NameConcept`s we want to be canonical.

Classes that contain fields, such as a `case class User(id: Id, age: Age, name: Name)`, can never know what implementations of type concepts they contain. If they did know their precise implementation, it would be impossible to update a `NameConcept` to a new implementation without also updating every piece of `User` functionality. This is a no-go, we want this to be automatic.

Therefore classes with fields must be designed as:

```scala
  trait User[Id <: UserIdConcept, Age <: AgeConcept, Name <: NameConcept] extends UserConcept {
    def id: Id
    def age: Age
    def name: Name
  }
```

### Generic classes

Generic classes are simpler than ordinary classes - they _already_ have their contents defined by a higher level of code. Here is `PairConcept` and it's implementation `Pair`:

```scala
  trait PairConcept[A, B] {
    def _1: A
    def _2: B
  }
 
  case class Pair[A, B](_1: A, _2: B) extends PairConcept[A, B]
```

### Writing a function

Because all code implementations can be replaced elsewhere, at any time, and everything you write is immutable, any function you write has to be future proof to the n-th degree. Literally no implementations from elsewhere can be allowed to exist in the function: The entire namespac available and all functionality the function needs access to must be passed in implicitly.

Here's a simple example, accessing the `age` field on `User`:

```scala
  implicit def UserToAge[Id <: UserIdConcept, Name <: NameConcept](
    implicit age: Canonical[AgeConcept]
  ): Fn[User[Id, age.Impl, Name], age.Impl] = Fn(_.age)
```

Accessing the `Age` field from `User` depends entirely on what the canonical `AgeConcept` is. So we pass this evidence in, and then we can return the right type (the compiler does the calculation for us).

Here's a more complex example, producing a `Pair` of name and age, from a given user:

```scala
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
```

First, we need to know what types we're talking about. So we retrieve our canonical `UserConcept` and name it as `User`, `AgeConcept` as `Age`, etc.

After that, we need to know how to access the `age` field of a `user` (`User ==>: Age`), how to get the name field (`User ==>: Name`), and finally how to build our `Pair` canonical: `Age ==>: Name ==>: Pair[Age, Name]`

Once we have this namespace, and all our building blocks of functionality, we can actually write our implementation in native scala, and it practically writes itself: `buildPair(getAge(user))(getName(user))`

You can think of this implicit list declared before any definition of `Fn`/`==>:` as declaring our variables/namespace. It tells the compiler, when building our program for us, that before we can talk about this functionality we need to have these things sorted out, available, computed.

At no time, ever, does a function talk about any scope beyond its implicit namespace. Only in this way can we make all imutable code reusable in all future scenarios.

### Writing a new version of a function

In `update5` there is a function `GetUserFromDb`, and in `update7` we write a new version of this, `GetUserFromDbV2`, which written in response to a new `NameConcept` (`FullName` rather than `LastName`).

The diff between these two user-from-db functions is:

```diff
- wrapName: String ==>: Name,
+ wrapName: (String, String) ==>: Name,
- Some(buildUser(id)(wrapAge(53))(wrapName("jeremy")))
+ Some(buildUser(id)(wrapAge(53))(wrapName("jeremy" -> "jackson")))
- Some(buildUser(id)(wrapAge(19))(wrapName("john")))
+ Some(buildUser(id)(wrapAge(19))(wrapName("john" -> "baptist")))
```

And that's all there is to it. We simply delcared in the implicits that we were changing the way we built our `NameConcept`, and then changed the simple usage sites within the body.

Updates are incredibly simple. The program is created for you by the scala compiler, you don't need complex threading of data through stacks of code.

If we plugged that function into our main stack (by importing it at orchestration time), the whole program would compile if the compiler found an implicit `(String, String) ==>: Name` defined for our chosen `Name` canonical.

## Differences from reality

In reality, the `update` packages as previously mentioned would be libraries, keyed by git hash.

In the file `Testing.scala`, there are several apps. These mimic different versions of the only mutable code allowed. Each `App` object has a docstring explaining the code development that drove the need for the update, and a short example of what the "real" diff would be. I left them all in side-by-side for ease of comparison, so nobody need dig through git commits.

The imports in the `App` objects are long and boring. In reality this would be solved by having a nested, shared package structure across the update libraries.

And, finally, choosing new function implementations is currently achieved by simply importing a different implicit definition. In reality, there would be a further `Canonical` typeclass for you to choose your canonical function definition explicitly (implicitly), from any concept to any concept.

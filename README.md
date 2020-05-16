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

When you've published you library, you mutate the main repository. This is the only mutable code in the entire project.

The main repository (`Testing.scala`) should be a small object, that starts your service and subscribes to implementations
found in the libraries you have added in a separate stream of work.

The main repository has access to all the libraries written by all developers on all branches of work. It can choose
any implementation it likes for class concepts and implicit function instances.

After you have changed the main repository to use your new code (by erasing the old import and adding your new one),
the compiler will verify that the entire stack of implicit code structure is compatible. If it is, it will build
the entirely new implementation for you automatically. Then we're good! On to the next piece of work.

If it's not compatible, the compilation will fail. At this stage, it's back to write a new update to fill the gap you left
in your implementations. 

This is conflict-free coding, abstracted over source control. At any point, the main program has access to all previous
code ever defined. New code is made with incremental additions, no bugs can ever be introduced in old code. Never again will you
have to compile 1,000s of source files every time you make a change, you will only ever have to compile new code, and the 
final verification.

Source control and incremental compilation, as code.

package io.typechecked.unity.fundamentals

trait Canonical[Concept] {
  type Impl <: Concept
}

object Canonical {
  type Aux[Concept, Impl0] = Canonical[Concept] { type Impl = Impl0 }
}

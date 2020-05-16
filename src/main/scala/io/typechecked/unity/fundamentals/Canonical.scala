package io.typechecked
package unity

trait Canonical[Concept] {
  type Impl <: Concept
}

object Canonical {
  type Aux[Concept, Impl0] = Canonical[Concept] { type Impl = Impl0 }
}

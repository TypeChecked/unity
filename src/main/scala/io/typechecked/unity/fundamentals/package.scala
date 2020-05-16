package io.typechecked.unity

package object fundamentals {
  type ==>:[A, B] = Fn[A, B]
  type |-->[A, B] = Canonical.Aux[A, B]
}

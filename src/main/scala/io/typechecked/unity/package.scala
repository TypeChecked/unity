package io.typechecked

package object unity {

  type ==>:[A, B] = Fn[A, B]

  type |-->[A, B] = Canonical.Aux[A, B]
}

package io.typechecked

package object unity {

  type ==>:[A, B] = Fn[A, B]

  type |-->[A, B] = Implementation.Aux[A, B]
}

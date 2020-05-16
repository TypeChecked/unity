package io.typechecked
package unity

trait Fn[A, B] { def apply(a: A): B }

object Fn {
  def apply[A, B](fn: A => B): Fn[A, B] = new Fn[A, B] { def apply(a: A): B = fn(a) }
}

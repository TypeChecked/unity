package io.typechecked.unity.update8

import io.typechecked.unity.fundamentals._
import io.typechecked.unity.update7.Implementations.FullName

object Functions {
  implicit def FullNameToStringCapitalized: FullName ==>: String = Fn { n =>
    s"${n.first.capitalize} ${n.last.capitalize}"
  }
}

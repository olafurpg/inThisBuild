---
category: blog
by: Ólafur Páll Geirsson
title: "Roadmap towards non-experimental macros"
date: 21-9-2017
---

This week, the Scala Center begins a new initiative to bring
non-experimental macros into the official scalac and dotc distributions.
This project will be developed in close collaboration with the Dotty
team at EPFL and Scala compiler team at Lightbend.
This initiative follows [SCP-014], a proposal that got approved with an
overwhelming majority at the Scala Center Advisory Board meeting last week.

## Old-style scala.reflect

Scala.reflect macros have become an integral part of the Scala 2.x ecosystem. 
Well-known libraries like ScalaTest, Sbt, Spark, Circe, Slick, Shapeless,
Spire and others use scala.reflect macros to achieve previously unreachable
standards of expressiveness, type safety and performance.

Unfortunately, scala.reflect macros have also gained notoriety as an arcane and
brittle technology.
The most common criticisms of scala.reflect is its subpar tooling support
and non-portable metaprogramming API based on Scala 2.x compiler internals.
Even five years after their introduction, scala.reflect macros still can't
expand in IntelliJ, leading to proliferation of spurious red squiggles -
sometimes in pretty simple code.
As a result of these known limitations, the language committee has decided to
retire the scala.reflect macro system.

## New-style Scalamacros

If you rely on scala.reflect macros, despair not.
During the last couple years, Eugene Burmako and his team have been working on
a new macro system that will support both Scala 2.x and Scala 3.x.
The name of this new macro system is "Scalamacros" and
and its source code is hosted at [scalamacros/scalamacros].

Scalamacros are based on a platform-independent metaprogramming API that was
designed with the following goals in mind:

- easy to use for macro authors, with common pitfalls guarded by the type
  system.
- portable across Scala 2.x, Dotty, IntelliJ Scala Plugin and other Scala
  compilers in the future.

To demonstrate the ease of using Scalamacros, let's implement a `fieldNames`
macro to extract the field names of a case class.

> Note. The following example uses a tentative Scalamacros API.

```scala
import scala.macros._
object CaseClass {
  def fieldNames[T]: List[String] = macro {
    val names = T.vals
       // fields of case classes have the "case" modifiers
      .filter(_.isCase)
      // construct string literal tree node
      .map(field => Lit.String(field.name.value))
    q"_root_.scala.collection.immutable.List(..$names)"
  }
}
```

Observe that a single `import scala.macros._` is enough to get started with Scalamacros.
Also, notice that we use quasiquotes and the signature of `fieldNames` contains
no compiler specific types.
We can test our macro works as expected

```scala
case class User(name: String, age: Int)
assert(List("name", "age") == CaseClass.fieldNames[User])
```
I hope this example got you excited about the potential for Scalamacros.

## Next steps

Following the recommendation of the Scala Center Advisory Board, the work on
Scalamacros will be an iterative processes between

1. implementing macro features that have been approved for inclusion into Scalamacros
2. gathering feedback from the community on what macro features merit inclusion
   in Scalamacros.

As for the first part, we immediately begin development for adding support for
a limited subset of blackbox def macros.

### Blackbox def macros

Blackbox def macros have non-controversially proven themselves to be invaluable
for the Scala community.
Examples of blacbox def macros include: `CaseClass.fieldNames` above, ScalaTest
`assert`, Play/Circe JSON automatic readers/writers and scala-logging info/warn/error.
Blackbox def macros share the following attributes:

- they're invoked at compile-time instead of runtime
- they can query the compiler for semantic information such as term types and
  symbols.
- from the end user's perspective, they look and behave much like regular Scala
  methods

These attributes of blackbox def macros enable them to mix naturally into
Scala codebases and play nicely with IDEs such as IntelliJ.
However, some scala.reflect macros rely on more advanced capabilities that
are not supported by blackbox def macros.
Most notably, these are whitebox def macros and macro annotations.

### Whitebox def macros

Whitebox def macros have not been approved for inclusion into Scalamacros.
Whitebox macros are similar to blackbox def macros with the distinction
that the result type of whitebox def macros can be narrowed from the call-site.
For example, imagine that we wish to implement a macro to convert case classes
into tuples.

```scala
import scala.macros._
object CaseClass {
  def toTuple[T](e: T): Product = macro { ??? }
  case class User(name: String, age: Int)
  // if blackbox: expected (String, Int), got Product
  // if whitebox: OK
  val user: (String, Int) = CaseClass.toTuple(User("Jane", 30))
}
```

Whitebox macros introduce several problems that blackbox macros don't
suffer from
- IDEs like IntelliJ are unable infer the correct type since they need
  to be able to invoke the macro body in order to typecheck the the expanded
  code.
- the typechecker needs to accommodate the inferred result types of expanded
  whitebox macros in all of the languages typing rules. For example, see
  [fundep materialization]. This impacts portability of the macro system.

### Macro annotations

Macro annotations have not been approved for inclusion into Scalamacros.
Macro annotations have the ability to synthesize publicly available definitions.
For example,
```scala
@json case class User(name: String, age: Int)

object MyApp {
  User("John", 18).toJson
  User.fromJson(""" { user: "John", age: 40 } """)
}
```

### Share your thoughts
If you are the author of a scala.reflect macro that , we invite you to start a thread
in [Scala Contributors] and mention

- what your macro does and why it's important for you
- what are the least powerful compiler APIs the macro requires
- justify why your macro can't be replaced by a compiler plugin or code
  generation.

[Scala Contributors]: https://contributors.scala-lang.org/
[fundep materialization]: https://docs.scala-lang.org/overviews/macros/implicits.html#fundep-materialization
[Scala Macros]: https://github.com/scalamacros/scalamacros
[scalamacros/scalamacros]: https://github.com/scalamacros/scalamacros
[minutes]: https://scala.epfl.ch/minutes/2017/09/12/september-12-2017.html
[SCP-014]: https://scala.epfl.ch/minutes/2017/09/12/september-12-2017.html#scp-014-production-ready-scalamacrosscalamacros

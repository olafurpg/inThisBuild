---
category: blog
by: Ólafur Páll Geirsson
title: "Roadmap towards non-experimental macros"
date: 21-9-2017
---

This week, the Scala Center begins a new initiative to bring
non-experimental macros into the official scalac and dotc distributions.
This project will be developed in close collaboration with the Scala community,
the Dotty team at EPFL, Scala compiler team at Lightbend and the IntelliJ Scala
Plugin team at Jetbrains.
This initiative follows [SCP-014], a proposal that got approved with an
overwhelming majority at the Scala Center Advisory Board meeting last week.

## Old-style: scala.reflect

Scala.reflect-based macros have become an integral part of the Scala 2.x ecosystem.
Well-known libraries like ScalaTest, Sbt, Spark, Circe, Slick, Shapeless,
Spire and others use scala.reflect macros to achieve previously unreachable
standards of expressiveness, type safety and performance.

Unfortunately, scala.reflect-based macros have also gained notoriety as an
arcane and brittle technology.
The most common criticisms of scala.reflect is its subpar tooling support
and non-portable metaprogramming API based on Scala 2.x compiler internals.
Even five years after their introduction, scala.reflect macros still can't
expand in IntelliJ, leading to proliferation of spurious red squiggles -
sometimes in pretty simple code.

Quoting Eugene Burmako, the author of Scala macros,
in [SIP-16] on self-cleaning macros

> While trying to fix these problems via evolutionary changes to the current
> macro system, we have realized that most of them are caused by the decision
> to use scala.reflect as the underlying metaprogramming API. Modelled after
> compiler internals, scala.reflect inherits many of its peculiar design
> choices. Extensive use of desugarings and existence of multiple independent
> program representations may have worked well for compiler development, but
> they turned out to be inadequate for a public API.

As a result of these known limitations, the language committee has decided to
retire the scala.reflect-based macro system.

## New-style: scala.macros

During the last couple years, Eugene Burmako and his team have been working on
a new macro system to address the limitations of scala.reflect-based macros.
Scalamacros are based on a platform-independent metaprogramming API that was
designed with the following goals in mind:

- small, the API should expose as little as possible from the compiler
- robust, common pitfalls such as hygiene are guarded by the type system
- portable, Scalamacros should work across Scala 2.x, Dotty, IntelliJ Scala
  Plugin as well as for other Scala compilers in the future.

To give you a taste of the tentative Scalamacros API, let's implement a `fieldNames`
macro to extract fields names of a case class

```scala
import scala.macros._
object CaseClass {
  def fieldNames[T]: List[String] = macro {
    val names = T.vals
       // fields of case classes have the "case" modifier
      .filter(_.isCase)
      // construct string literal tree node for each field
      .map(field => Lit.String(field.name.value))
    q"_root_.scala.List(..$names)"
  }
}
```

Observe that a single `import scala.macros._` is enough to get started with
Scalamacros.
Also, notice that the macro definition and implementation are merged and that
quasiquotes are supported.
We can test our macro works as expected

```scala
case class User(name: String, age: Int)
assert(List("name", "age") == CaseClass.fieldNames[User])
```
I hope this example got you excited about the potential for Scalamacros.
The demonstrated APIs are only tentat

## Next steps

Scalamacros is a large undertaking, involving a collaboration between many
different parties.
Here is a roughly estimated timeline for the project:

- in Dotty, Liu Fengyun a Phd student at EPFL will work on adding support
  for Scalamacros as soon as possible.
- in IntelliJ, Mikhail Mutcianko from the Scala Plugin team at Jetbrains will
  work on adding support for Scalamacros as soon as possible.
- in Scala 2.12, we experiment with Scalamacros via compiler plugins
  as soon as possible.
- in Scala 2.13, we continue to experiment with Scalamacros via compiler
  plugins and compiler feature flags in later minor releases
- in Scala 2.14 Scalamacros no longer “experimental” and scala.reflect is
  deprecated

Following the recommendation of the Scala Center Advisory Board, the work on
Scalamacros will be an iterative processes between

-  implementing macro features that have been approved for inclusion into
   Scalamacros
-  gathering feedback from the community on what macro features merit inclusion
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
- they can query the compiler for semantic information such as types and symbols.
- from the end user's perspective, they look and behave much like regular Scala
  methods

These attributes of blackbox def macros enable them to mix naturally into
Scala codebases and play nicely with IDEs such as IntelliJ without
customizations.

### SIP proposal

Alongside adding support for a limited set of blackbox def macros,
we will prepare a SIP proposal for making Scalamacros an official Scala
language feature.

### Documentation


### Share your feedback
Some scala.reflect macros rely on advanced capabilities beyond what
blackbox macros support.
Most notably, these are whitebox def macros and macroa annotations.
If you are the author of a scala.reflect macro that 
, we invite you to start a thread
in [Scala Contributors] and mention

- what your macro does and why it's important for you
- what are the least powerful compiler APIs the macro requires
- justify why your macro can't be replaced by a compiler plugin or code
  generation.

#### Whitebox def macros

Whitebox def macros have neither been approved nor rejected for inclusion into
Scalamacros.
Whitebox macros are similar to blackbox def macros with the distinction
that the result type of whitebox def macros can be narrowed at each call-site.
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

Whitebox macros are more powerful than blackbox def macros.
A whitebox macro that declares its result type as `Any`
can have it's result type inferred to a custom precise type at every call-site.
If such a whitebox macro is marked implicits for example, it needs to be expanded
first in order to be disqualified as an implicit candidate.

Quoting Eugene Burmako in [SIP-28] on inline/meta

> The main motivation for getting rid of whitebox expansion is simplification -
> both of the macro expansion pipeline and the typechecker.  Currently, they
> are inseparably intertwined, complicating both compiler evolution and tool
> support.

Quoting the [minutes from the Scala Center Advisory Board][SCP-014]:

> Dotty, he [Martin Odersky] says, wants to be a “capable language” rather than
> a “language toolbox”. So it matters whether whitebox macros are being used to do
> “Scala-like” things, or to turn Scala into something else. So “we will have
> to look at each one” of the ways whitebox macros are being used. 

Adriaan Moors, the Scala compiler team lead at Lightbend agreed.

If you want to see whitebox macros approved for inclusion in Scalamacros,
we invite you to share your feedback in [Scala Contributors].

#### Macro annotations

Macro annotations have neither been approved nor rejected for inclusion into
Scalamacros.
Macro annotations have the ability to synthesize publicly available definitions.
For example, the `@json`
```scala
@json case class User(name: String, age: Int)

object MyApp {
  User("John", 18).toJson
  User.fromJson(""" { user: "John", age: 40 } """)
}
```

[Scala Contributors]: https://contributors.scala-lang.org/
[fundep materialization]: https://docs.scala-lang.org/overviews/macros/implicits.html#fundep-materialization
[Scala Macros]: https://github.com/scalamacros/scalamacros
[scalamacros/scalamacros]: https://github.com/scalamacros/scalamacros
[minutes]: https://scala.epfl.ch/minutes/2017/09/12/september-12-2017.html
[SCP-014]: https://scala.epfl.ch/minutes/2017/09/12/september-12-2017.html#scp-014-production-ready-scalamacrosscalamacros
[SIP-16]: https://github.com/scala/docs.scala-lang/pull/57#issuecomment-239210760

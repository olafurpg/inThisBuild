---
category: blog
by: Ólafur Páll Geirsson
title: "Roadmap towards non-experimental macros"
date: 21-9-2017
---

This week, the Scala Center begins a new initiative to establish a
non-experimental macro system into the official scalac, dotc and intellij-scala
distributions.
This project will be developed in close collaboration with the Scala community,
the Dotty team at EPFL, Scala compiler team at Lightbend and the IntelliJ Scala
Plugin team at Jetbrains.
This initiative follows [SCP-014], a proposal that got approved with an
overwhelming majority at the Scala Center Advisory Board meeting last week.

## Brief history
Before diving into the roadmap of this new exciting project, I'd like to briefly
summarize and recognize the efforts that have been made so far towards
establishing a standard, non-experimental macro system for Scala.
I'll try to keep it short here, for a more comprehensive coverage please refer
to the list of [papers written by Eugene Burmako][papers], the founder of Scala
Macros, in particular his PhD thesis defense [Unification of Compile-Time and
Runtime Metaprogramming in Scala][thesis], which covers the same history with more
details.

If you want the TL;DR, see [next steps](#next-steps).

### v2: scala.reflect

Scala.reflect-based macros are an integral part of the Scala 2.x ecosystem.
Well-known libraries like ScalaTest, Sbt, Spark, Circe, Slick, Shapeless,
Spire and others use scala.reflect macros to achieve previously unreachable
standards of expressiveness, type safety and performance.

Unfortunately, scala.reflect-based macros have also gained notoriety as an
arcane and brittle technology.
The most common criticisms of scala.reflect is its sub-par tooling support
and non-portable metaprogramming API based on Scala 2.x compiler internals.
Even five years after their introduction, scala.reflect macros still can't
expand in IntelliJ, leading to proliferation of spurious red squiggles -
sometimes in pretty simple code.

Quoting Eugene Burmako, the author of Scala macros, in [SIP-16] on
self-cleaning macros

> While trying to fix these problems via evolutionary changes to the current
> macro system, we have realized that most of them are caused by the decision
> to use scala.reflect as the underlying metaprogramming API. Modelled after
> compiler internals, scala.reflect inherits many of its peculiar design
> choices. Extensive use of desugarings and existence of multiple independent
> program representations may have worked well for compiler development, but
> they turned out to be inadequate for a public API.

As a result of these known limitations, the language committee has decided to
retire the scala.reflect-based macro system.
Another justification for retiring the scala.reflect-based macro system was
that a new macro system based on Scalameta was "just around the corner".

### v3: scala.meta
TODO(olafur) point out SIP-29

The [Scalameta] project was founded to become a better macro system for Scala,
with the vision to replace scala.reflect as the de-facto metaprogramming
toolkit for Scala.

With Scalameta, we managed to support macro annotations in
[Scala 2.x](http://scalameta.org/paradise/),
[IntelliJ Scala plugin](https://blog.jetbrains.com/scala/2016/11/11/intellij-idea-2016-3-rc-scala-js-scala-meta-and-more/)
and
[Dotty](https://github.com/liufengyun/eden).
The novelty with Scalameta macros was that they converted compiler-specific
ASTs into the [Scalameta AST], which is is a large collection "dumb" data
containers that leak no implementation details from the compiler.

In Scalafmt, I use Scalameta macro annotations to generate readers for
over 
[90 different configuration options](http://scalameta.org/scalafmt/#Configuration)
into a single case class
<blockquote class="twitter-tweet" data-lang="en"><p lang="en" dir="ltr">New style &quot;inline/meta&quot; macro merged into scalafmt, Dotty-ready macros are happening and they are amazing! <a href="https://twitter.com/hashtag/scala?src=hash">#scala</a> <a href="https://github.com/scalameta/scalafmt/pull/459">scalameta/scalafmt#459</a> <a href="https://t.co/ZvjdWE0Gp3">pic.twitter.com/ZvjdWE0Gp3</a></p>&mdash; Ólafur Páll Geirsson (@olafurpg) <a href="https://twitter.com/olafurpg/status/779372897198637057">September 23, 2016</a></blockquote>
<script async src="//platform.twitter.com/widgets.js" charset="utf-8"></script>
Many other OSS libraries also use Scalameta macro annotations to build amazing
applications:
- [Freestyle](http://frees.io/):  cohesive & pragmatic framework of FP centric
  Scala libraries
- [Stalagmite](https://vovapolu.github.io/scala/stalagmite/perf/2017/09/02/stalagmite-performance.html):
  effective and customizable replacement of conventional case classes with
  convenient optimizations.
- [Mainecoon](http://kailuowang.com/mainecoon/): A library for transforming and
  composing tagless final algebras
- [Dilate](https://github.com/vitorsvieira/dilate) /
  [Newtypes](https://github.com/alexknvl/newtypes): better value
  classes/newtypes for Scala.
- [Example.scala](https://static.javadoc.io/com.thoughtworks.example/unidoc_2.12/2.0.0/com/thoughtworks/example.html):
  Generate unit tests from Scaladoc strings

The Scala community is indeed creative and eager to explore new metaprogramming
facilities to maximize type safety, expressiveness and performance.

Scalameta macro annotations are far from perfect, they suffer from integration
problems with Scaladoc, Scala IDE/presentation compiler, 
Scala REPL and other compiler plugins such as Scoverage.
Most importantly however, we hit fundamental roadblockers when trying to add
support for def macros with access to the semantic API.
Nevertheless, we learned a few important lessons

- Users and macro authors care a lot about IDE support, in particular IntelliJ
  support.
- Building a portable macro system with ASTs that don't leak compiler
  implementation details is possible.
- The Scalameta ASTs, which comprehensively cover all syntactic details
  of Scala syntax, may be great to build developer tools such as scalafmt
  but it ise too detailed for macro.
  In particular, the detailed AST nodes introduced portability problems during
  later phases of the compilation pipeline.

With these lessons learned, we regretfully decided to retire our efforts to
build a macro system on top of Scalameta.
From now on, Scalameta's primary focus is to support building developer tools such
as Scalafmt and Scalafix.

### v4: scala.macros

This spring, Eugene Burmako at Twitter and Liu Fengyun at EPFL worked on a
new macro system to address the limitations of scala.reflect-based and
scala.meta-based macros.
The result of this multi-year effort is
hosted at [scalamacros/scalamacros] and its design is explained in detail in the
technical report
["Two approaches to portable macros"](https://www.dropbox.com/s/2xzcczr3q77veg1/gestalt.pdf).
This fourth iteration of Scala Macros builds on top of the strengths of the
scala-reflect API with the following distinction, it's

- smaller, the API exposes as little as possible from the compiler
  while still being able to support most interesting macro applications.
- more robust, common pitfalls such as hygiene are guarded by the type system
  enabled by separation of typed and untyped trees.

Scala Macros v4 build on the same foundation as scala.reflect by providing an
API to lazily extract and construct Scala syntax.
Moreover, it is no longer necessary to pass around path dependent
trees or contexts, avoiding the need for macro bundles.
Finally, the def macro definition and implementation are merged into a single
`def name = macro` definition, simplifying the macro expansion engine and
preventing common mistakes made by novice macro authors.

To give you a taste of the tentative macros v4 API, let's implement a `fieldNames`
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
macros v4.
Also, notice that quasiquotes are supported.
We can test our macro works as expected

```scala
case class User(name: String, age: Int)
assert(List("name", "age") == CaseClass.fieldNames[User])
```
I hope this example is enough to get you excited about the potential for Scala
Macros v4.
The demonstrated APIs are only tentative, and must pass thorough
[SIP] review before being approved for inclusion as part of
the Scala Language Specification.

## Next steps

As the history above shows, establishing a stable and non-experimental macro
system for Scala is a large undertaking.
It has taken multi-year efforts to reach where we are today, involving a
collaboration between many different parties.
We still have a long way to go to reach the level of expressiveness, robustness
and simplicity that we seek in a stable macro system.

Here below is a rough estimated roadmap for macros v4

- in Scala 2.12, we experiment with macros v4 via compiler plugins
  as soon as possible.
- in Dotty, Liu Fengyun at EPFL will work on adding support for macros v4 as
  soon as possible.
- in IntelliJ, Mikhail Mutcianko from the Scala Plugin team at Jetbrains will
  work on adding support for macros v4 as soon as possible.
- in Scala 2.13, we continue to experiment with macros v4 via compiler
  plugins and compiler feature flags in later minor releases
- in Scala 2.14 macros v4 no longer “experimental” and scala.reflect is
  deprecated

Following the recommendation of the Scala Center Advisory Board, the work on
macros v4 will be an iterative processes between

-  implementing macro features that have been approved for inclusion into
   macros v4
-  gathering feedback from the community on what macro features merit inclusion
   in macros v4

As for the first part, we immediately begin development for a limited subset of
blackbox def macros.

### Blackbox def macros

Blackbox def macros have non-controversially proven themselves to be invaluable
for the Scala community.
Examples of blackbox def macros include: `CaseClass.fieldNames` above,
ScalaTest `assert` and Play/Circe JSON automatic readers/writers.
Blackbox def macros share the following attributes:

- they're invoked at compile-time instead of runtime
- they can query the compiler for semantic information such as types and symbols.
- they faithfully respect their declared type signatures, making their
  implementation irrelevant to understand their behavior. From the end user's
  perspective, they look and behave much like regular Scala methods

These attributes of blackbox def macros enable them to mix naturally into
Scala codebases and prime for inclusion in the language specification.

### SIP proposal

Alongside prototyping preliminary support for a limited set of blackbox def
macros, we will immediately start preparing a SIP proposal to include macros
v4 into the Scala Language Specification.
We plan to address the valuable reviews made to [SIP-29] on inline/meta in a
new proposal.
In addition, we will document how we we aim to solve hygiene, which plagues
most macro tutorials and was previously considered to be out of scope for
SIP-29, using an innovation discovered by Liu Fengyun and Eugene Burmako.

### Documentation

There is missing comprehensive documentation that outlines the wide
landscape of metaprogramming facilities that are available to Scala developers.
While discussing macros with members of the OSS community and industry,
I repeatedly get questions such as:
should I use def macros, annotations, shapeless, a compiler plugin or use code
generation?
There is no silver bullet, each solution comes with a set of trade-offs.

We believe good documentation is critical to the success of macros v4.
Especially, we want to document for which use-cases macros are suitable,
and for which use-cases they're either overkill, or insufficient.
Macros should in general be used as a last resort.
We care deeply to know what you'd like to see covered in this effort to map
available metaprogramming abstractions in Scala.

### Share your feedback
The current set of approved features in macros v4 does not reach feature parity
with the scala.reflect-based macro system.
Some scala.reflect macros rely on advanced capabilities beyond what
blackbox macros support.
Most notably, these features include (but are not limited to) whitebox def
macros and macro annotations.

By not supporting these advanced features, we put ourselves a fragile situation
where we risk forcing Scala users to remain on old versions of the compiler.
Such a situation is undesirable for both Scala users and those who wish to
evolve the language.
We must debate together as a community whether these advanced features
merit inclusion in the language specification or if they can be replaced
with alternative metaprogramming techniques.

Here below I try to summarize the pros and cons of whitebox def macros
and macro annotations.
I invite you to invite you to start a thread in [Scala Contributors] and
share your personal thoughts.
In particular, we want to explore

- what does your macro do and why is ti important for you and your users?
- can you use alternative metaprogramming techniques such as
  old-fashioned code generation or compiler plugins to achieve the same
  functionality?

#### Whitebox def macros

Whitebox def macros have neither been approved nor rejected for inclusion into
macros v4.
Whitebox macros are similar to blackbox def macros with the distinction
that the result type of whitebox def macros can be refined at each call-site.
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
first in order to be disqualified as a candidate during implicit search.

Quoting Eugene Burmako in [SIP-29] on inline/meta

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

TODO(olafur) acknowledge important use-cases for whitebox

- fundep materialization -> compiler plugin?

If you want to see whitebox macros approved for inclusion in macros v4,
we invite you to share your feedback in [Scala Contributors].

#### Macro annotations

Macro annotations have neither been approved nor rejected for inclusion into
macros v4.
Macro annotations have the ability to synthesize publicly available definitions.
For example, the `@json`

```scala
@json class User(name: String, age: Int)

object MyApp {
  User("John", 18).toJson
  User.fromJson(""" { user: "John", age: 40 } """)
}
```

Acknowledge important use-cases for macro annotations

- public type providers 
  - simulacrum, frees
  - can they be replaced with code generation?

-
  - docstrings
  - synthetic

## Acknowledgements

I would like to express my great gratitude to Eugene Burmako and his

[Scalameta AST]: https://github.com/scalamacros/scalamacros/blob/master/core/src/main/scala/scala/macros/trees/Trees.scala
[Scalameta]: https://infoscience.epfl.ch/search?ln=en&p=Burmako%2C+Eugene&jrec=1&f=author
[papers]: https://infoscience.epfl.ch/search?ln=en&p=Burmako%2C+Eugene&jrec=1&f=author
[thesis]: https://infoscience.epfl.ch/record/226166/files/EPFL_TH7159.pdf
[Scala Contributors]: https://contributors.scala-lang.org/
[fundep materialization]: https://docs.scala-lang.org/overviews/macros/implicits.html#fundep-materialization
[Scala Macros]: https://github.com/scalamacros/scalamacros
[scalamacros/scalamacros]: https://github.com/scalamacros/scalamacros
[minutes]: https://scala.epfl.ch/minutes/2017/09/12/september-12-2017.html
[SCP-014]: https://scala.epfl.ch/minutes/2017/09/12/september-12-2017.html#scp-014-production-ready-scalamacrosscalamacros
[SIP-16]: https://github.com/scala/docs.scala-lang/pull/57#issuecomment-239210760
[SIP-29]: https://docs.scala-lang.org/sips/inline-meta.html#losing-whiteboxity
[SIP]: https://docs.scala-lang.org/sips/index.html

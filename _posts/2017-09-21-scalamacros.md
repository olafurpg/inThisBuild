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

### v1: scala.reflect

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

### v2: scala.meta

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
The details of this approach are explained in more detail in [SIP-29], a
proposal to use Scalameta as the foundation for building macros in Scala.

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

### v3: scala.macros

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
- more robust, with common pitfalls in scala.reflect-based macros guarded by
  the type system with separation of typed and untyped trees.

Scala Macros v3 build on top of a similar design as scala.reflect by providing
an API to lazily extract and construct Scala syntax.
It is no longer necessary to pass around path dependent trees or contexts,
avoiding the need for macro bundles.
Moreover, the def macro definition and implementation are merged into a single
`def name = macro` definition, simplifying the macro expansion engine and
preventing common mistakes made by novice macro authors.

One paint point in macros v2 that macros v3 does not address is the separate
compilation restriction.
Macro definitions must still be compiled in a separate project from where they
are used.
Nevertheless, we believe that v3 represents a significant enough improvement to
forgive this restriction, which is admittedly an inconvenience for macro author
but arguably not a blocker for adoption.

To give you a taste of the tentative macros v3 API, let's implement a `fieldNames`
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
macros v3.
Also, notice that quasiquotes are supported.
We can test our macro works as expected

```scala
case class User(name: String, age: Int)
assert(List("name", "age") == CaseClass.fieldNames[User])
```
I hope this example is enough to get you excited about the potential for Scala
Macros v3.
The demonstrated APIs are only tentative, and must pass thorough
[SIP] review before being approved for inclusion as part of
the Scala Language Specification.

## Next steps

As the history above shows, establishing a stable macro system for Scala is a
large undertaking.
It has taken multi-year efforts to reach where we are today, involving a
collaboration between many different parties.
We still have a long way to go to reach the level of expressiveness, robustness
and simplicity that we seek in a stable macro system.

Here below is a rough estimated roadmap for macros v3

- in Scala 2.12, we experiment with macros v3 via compiler plugins
  as soon as possible.
- in Dotty, Liu Fengyun at EPFL will work on adding support for macros v3 as
  soon as possible.
- in IntelliJ, Mikhail Mutcianko from the Scala Plugin team at Jetbrains will
  work on adding support for macros v3 as soon as possible.
- in Scala 2.13, we continue to experiment with macros v3 via compiler
  plugins and compiler feature flags in later minor releases
- in Scala 2.14 macros v3 no longer “experimental” and scala.reflect is
  deprecated

Following the recommendation of the Scala Center Advisory Board, the work on
macros v3 will be an iterative processes between

-  implementing macro features that have been approved for inclusion into
   macros v3
-  gathering feedback from the community on what macro features merit inclusion
   in macros v3

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
Scala codebases and prime for inclusion in the Scala Language Specification.

### SIP proposal

Alongside prototyping preliminary support for a limited set of blackbox def
macros, we will immediately begin preparing a SIP proposal to include macros
v3 into the Scala Language Specification.
We plan to address the valuable reviews made to [SIP-29] on inline/meta in a
new proposal, so that SIP-29 can be superseded by our new proposal.
In addition, we will document how we we aim to solve hygiene using an
innovation discovered by the collaboration of Liu Fengyun and Eugene Burmako.
Hygiene plagues most macro tutorials and was previously considered to be out of
scope for SIP-29

### Documentation

Currently, there exists no official comprehensive documentation that outlines
the wide landscape of metaprogramming facilities that are available to Scala
developers.
While discussing macros with members of the OSS community and industry,
I repeatedly receive questions such as:
should I use def macros, annotations, shapeless, a compiler plugin or code
generation?
There is no silver bullet, each solution comes with a set of trade-offs.

We believe good documentation is critical to the success of macros v3.
Especially, we want to document for which use-cases macros are suitable,
and for which use-cases they're either overkill, or insufficient.
Macros should in general be used as a last resort.
We care deeply to know what you'd like to see covered in this effort to map
available metaprogramming abstractions in Scala.
Please share your thought.

### Share your feedback
The current set of approved features in macros v3 does not reach feature parity
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

- what does your macro do and why is it important for your users?
- can you use alternative metaprogramming techniques such as
  code generation scripts or compiler plugins to achieve the same
  functionality?

#### Whitebox def macros

Whitebox def macros have neither been approved nor rejected for inclusion into
macros v3.
Whitebox macros are similar to blackbox def macros with the distinction
that the result type of whitebox def macros can be refined at each call-site.
The ability to refine the result types opens up exciting applications including
- [fundep materialization], used by shapeless Generic
- [extractor macros], used by quasiquotes in scala.reflect, scala.meta, and
scala.macros
- [anonymous type providers]

To give an example of how blackbox and whitebox macros differ, imagine that we
wish to implement a macro to convert case classes into tuples.

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

As you can see from this example, whitebox macros are more powerful than
blackbox def macros.
A whitebox macro that declares its result type as `Any` can have it's result
type refined to any precise type in the Scala typing lattice.
This powerful capability opens up questions.
For example, do implicit whitebox def macros always need to be expanded in
order be disqualified as a candidate during implicit search.

Quoting Eugene Burmako from [SIP-29] on inline/meta, which contains a detailed
analysis on "Loosing whiteboxity"

> The main motivation for getting rid of whitebox expansion is simplification -
> both of the macro expansion pipeline and the typechecker.  Currently, they
> are inseparably intertwined, complicating both compiler evolution and tool
> support.

Note, however, that the portable design of macros v3 makes it possible to infer
the correct result types for whitebox macros in IDEs such as IntelliJ.

Quoting the [minutes from the Scala Center Advisory Board][SCP-014]:

> Dotty, he [Martin Odersky] says, wants to be a “capable language” rather than
> a “language toolbox”. So it matters whether whitebox macros are being used to do
> “Scala-like” things, or to turn Scala into something else. So “we will have
> to look at each one” of the ways whitebox macros are being used. 

Adriaan Moors, the Scala compiler team lead at Lightbend agreed with Martin,
and mentioned a current collaboration with Miles Sabin to improve scalac so
that Shapeless and other libraries can rely less on macros and other
nonstandard techniques

If you want to see whitebox macros approved for inclusion in macros v3,
we invite you to share your thoughts in [Scala Contributors].

#### Macro annotations

Macro annotations have neither been approved nor rejected for inclusion into
macros v3.
Macro annotations have the ability to synthesize publicly available definitions,
accommodating the [public type provider] pattern.
Popular macro annotation libraries include
- [simulacrum](https://github.com/mpilquist/simulacrum) first-class syntax
  support for type classes in Scala
- [Freestyle](http://frees.io/): cohesive & pragmatic framework of FP centric
  Scala libraries

To show an example macro annotation, consider the following `@deriving` macro
annotation from the library [Stalactite]

```scala
@deriving(Encoder, Decoder)
case class User(name: String, age: Int)

// expands into
case class User(name: String, age: Int)
object User {
  implicit val encoder: Encoder[User] = DerivedEncoder.gen
  implicit val decoder: Decoder[User] = DerivedDecoder.gen
}
```
The unique feature of macro annotations is that they can synthesize publicly
available definitions such as `User.encoder/decoder`.
Neither whitebox or blackbox def macros have this capability.
It is generally considered best practice to place implicit
typeclass instances in the companion object.
This pattern significantly improves on compile times and prevents code bloat
compared to full-derivation at each call-site.

Surely, it's possible to manually write out the
`implicit val decoderBar: Decoder[Bar] = DerivedDecoder.gen`
parts.
However, for large business applications with many domain-specific data types
and typeclasses, such boilerplate hurts readability and often falls prey to
typos, leading to bugs.

Old-fashioned code generation via scripting can used as an
alternative to macro annotations.
On the other hand, such code generation traditionally comes with a non-trivial
build tax.
Maybe it's possible to provide better tools for traditional code generation
via scripting, replacing the needs for macro annotations.
We are interested in hearing your opinions.

If you want to see macro annotations approved for inclusion in macros v3, we
invite you to share your thoughts in [Scala Contributors].

## Acknowledgements

On behalf of the Scala Center,
I would like to express my great gratitude to Eugene Burmako and his relentless
work to make macros in Scala as capable and popular as they are today.
Eugene has been a steward of macros in Scala for over 6 years.
During both professional and personal time, he has generously worked on
exploring new metaprogramming paradigms, mentored dozens of people (including
myself!) and communicated his findings with the Scala community both online and
offline.
We are honored to take the lead from your solid guidance.
I look forward to continuing our fruitful collaboration and I hope we can stand
up to the challenge to complete this project to end.

[Scalameta AST]: https://github.com/scalamacros/scalamacros/blob/master/core/src/main/scala/scala/macros/trees/Trees.scala
[Scalameta]: https://infoscience.epfl.ch/search?ln=en&p=Burmako%2C+Eugene&jrec=1&f=author
[papers]: https://infoscience.epfl.ch/search?ln=en&p=Burmako%2C+Eugene&jrec=1&f=author
[thesis]: https://infoscience.epfl.ch/record/226166/files/EPFL_TH7159.pdf
[Scala Contributors]: https://contributors.scala-lang.org/
[fundep materialization]: https://docs.scala-lang.org/overviews/macros/implicits.html#fundep-materialization
[anonymous type providers]: http://docs.scala-lang.org/overviews/macros/typeproviders.html#anonymous-type-providers
[extractor macros]: http://docs.scala-lang.org/overviews/macros/extractors.html
[public type provider]: http://docs.scala-lang.org/overviews/macros/typeproviders.html#public-type-providers
[Scala Macros]: https://github.com/scalamacros/scalamacros
[scalamacros/scalamacros]: https://github.com/scalamacros/scalamacros
[Stalactite]: https://gitlab.com/fommil/stalactite
[minutes]: https://scala.epfl.ch/minutes/2017/09/12/september-12-2017.html
[SCP-014]: https://scala.epfl.ch/minutes/2017/09/12/september-12-2017.html#scp-014-production-ready-scalamacrosscalamacros
[SIP-16]: https://github.com/scala/docs.scala-lang/pull/57#issuecomment-239210760
[SIP-29]: https://docs.scala-lang.org/sips/inline-meta.html#losing-whiteboxity
[SIP]: https://docs.scala-lang.org/sips/index.html

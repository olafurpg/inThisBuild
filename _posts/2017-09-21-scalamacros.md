---
category: blog
by: Ólafur Páll Geirsson
title: "Roadmap towards non-experimental and portable macros"
date: 21-9-2017
---

Starting next week, the Scala Center begins a new initiative to bring
non-experimental macros into the official scalac and dotc distributions.
This project will be developed in close collaboration with the Dotty
(Scala 3.x) at EPFL and Scala 2.x compiler core team at Lightbend.
This initiative follows [SCP-014], a proposal that got approved with an
overwhelming majority at the Scala Center Advisory Board meeting last week.

## Old-style scala.reflect

Scala.reflect def macros have become an integral part of the Scala 2.x ecosystem. 
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
retire the scala.reflect macro system in Dotty.

## New-style Scalamacros

If you rely on scala.reflect macros, despair not.
During the last couple years, we've been working on a new macro system that
will support both Scala 2.x and Scala 3.x.
The name of this new macro system is simply "Scalamacros",
and it source code is hosted at [scalamacros/scalamacros].

Scalamacros are based on a platform-independent metaprogramming API that was
designed with the following goals in mind:

- easy to use for macro authors, with common pitfalls guarded by the type
  system.
- portable across Scala 2.x, Dotty and other Scala language compilers in
  the future.
- great tooling support

Following recommendation of the Scala Center Advisory Board, the work on
Scalamacros will be an iterative processes between 

1. implementing approved macro features and
2. gathering feedback from the community on what macro features merit inclusion
   in Scalamacros.

As for the first part, we will immediately begin development for adding support
for a limited subset of blackbox def macros.

### Blackbox def macros

### Community feedback



[scalamacros/scalamacros]: https://github.com/scalamacros/scalamacros
[minutes]: https://scala.epfl.ch/minutes/2017/09/12/september-12-2017.html
[SCP-014]: https://scala.epfl.ch/minutes/2017/09/12/september-12-2017.html#scp-014-production-ready-scalamacrosscalamacros

# Spineless Tagless G-Machine implemented in Java

This project is part of the *CS5341* course at the University of Applied Sciences, THM in Gießen, Germany.
The course is about kernel architectures and programming languages using those architectures and was held by [Prof. Dr.-Ing. Dominikus Herzberg](https://github.com/denkspuren) in the winter term 2021/2022.

This repository provides a virtual machine implementation of the *Spineless Tagless G-machine* (short STG) in Java 17.
The [source code for the machine itself](stg/src) as well as some simple [example programs](stg/examples) can be found within this repository.
The [documentation written for this course](documentation/Ausarbeitung.pdf) is only available in german.

## Build & Run

To build and run the software provided with this project, it is necessary to have Java 17 (or a newer version) installed.

The executable JAR can either be downloaded from the [Releases section of this repository](https://github.com/Niklas-Deworetzki/java-stg/releases) or build directly using Maven.
If you got a version of the JAR running it with the `--help` command line flag should print a standard help page, describing the executable:

```
java -jar path/to/stg.jar --help
```

Running the JAR without arguments will allow you to type in a STG program by hand, as the parser will read from standard input.
It is however more comfortable to provide one (or multiple) input files, which are then loaded, parsed, analyzed and executed.

```
java -jar path/to/stg.jar stg/examples/nats.stg
```

This program should run and print the number `55` on your screen.
It does this by creating a list of **all** natural numbers and summing the first 10 of them.

### Building from source

It is possible to build an executable JAR from source.
To do this, only a [Maven](https://maven.apache.org/) installation is required in addition to Java 17.
The source code project root is in the `stg/` directory.
So from the repository root, the following command should switch to the source code directory and build an executable JAR over there.

```
cd stg/
mvn package
```

The executable JAR will be generated as `target/stg-1.0.jar` (or `stg/target/stg-1.0.jar`) from the repository root.

## What is the STG?

STG describes the *Spineless Tagless G-machine* as well as the *STG-Language*, a small programming language to program this machine.
This abstract machine can be used as an execution model to run more abstract programs on a low-level machine.
The main purpose of the machine is the execution of non-strict higher-order functional programs without (or with only little) overhead.
There are many challenges in running programs of these programming languages, as there is a large semantic difference between them and the basic notions of modern, imperative computer hardware.
Delayed execution, anonymous functions (lambda expression) and higher-order functions are concepts entirely unknown to a simple register machine.
The STG bridges the gap of these semantic differences.

In contrast to most other abstract machines, the STG is programmed using a functional programming language (the STG-language).
Instead of describing the changed made to the machine state using a sequence of primitive instructions, declarations describe the executed program as a graph.
This graph is then step by step reduced during runtime, making the STG-machine a *graph reduction machine*.

The STG-machine provides some improvements in contrast to other graph reduction machines.
These improvements give it its rather cryptic name: Spineless tagless G(raph reduction)-machine

**Spineless** in this context refers to the spine, an explicit data structure representing the graph to be reduced.
This spine is not explicitly constructed, but instead present implicitly using pointers to code fragments and selected closures on the heap.
Not having the whole program as a graph in memory obviously reduces the memory consumption of the machine.
As an additional advantage, the need of rearranging the graph nodes on a stack or a similar data structure is also eliminated, making reduction steps more efficient.

**Tagless** refers to the graph nodes and their representation.
Similar graph reduction machines distinguish different kinds of graph nodes and the appropriate reduction step by examining a tag.
For example a tag could be present in a node, telling a machine whether this node is unevaluated and has to be forced as a next step, or if the node was already evaluated and contains a value.
The STG-machine uses a different approach, similar to the dynamic dispatch strategy used in many object-oriented programming languages.
Nodes simply have a code-pointer as part of their structure, which refers to the appropriate action associated with the node.
Executing this action is then simply done by following the pointer and jumping to a piece of code implementing said action.
An evaluated node then points to code, that will return the evaluated value, while unevaluated nodes will continue evaluation.

The approaches used by the STG allow such an efficient translation of non-strict functional programs, that [the GHC Haskell compiler uses it for code generation.](https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/compiler/generated-code)

## Using the STG-language

The syntax for the STG-language used within this project is largely inspired by the originally proposed syntax.
However, there are a few adaptations that allow easier (and more decidable) parsing.

### Program structure and lambda forms

On the top level, a program consists of a set of lambda forms bound to distinct names.
They are collected from all input programs provided as well as the [prelude](stg/src/main/resources/Prelude.stg) bundled with the executable.
**If you wish to not include the definitions of the prelude, pass the `-n` flag to the executable.**

Lambda forms in the STG-language have a few extra features, that differentiate them from classical lambda expressions.
The basic syntax is:

```
name = \ { freeVar1 freeVar2 ... } param1 param2 ... -> Expression
```

All variable names start with a lower-case letter and cannot include a `#` character.
Apart from whitespace they can include any arbitrary printable characters.

A lambda form is introduced by using the `\` character, which represents the *Lambda* symbol, followed by a list of free variables between curly parentheses.
These free variables have to be defined, as they will be present in the heap allocated closure and therefore change the machine semantics.
**It is, however, strongly recommended to use the `-e infer-free` option when executing programs, as this will allow you to omit the free variable list.**

```
name = \ param1 param2 ... -> Expression
```

The following sections describe the different valid expressions.

### Function Applications

Function Applications are written as a function name followed by a list of arguments between two parenthesis.
If there aren't any arguments, the parenthesis can be omitted.

```
// Application of the function f to three arguments x, y and z.
f(x y z)

// Application of the primitive function +# to two primitive arguments.
+#(1# 2#)

// Application of the constructor Cons to two arguments.
Cons(head tail)
```

There are a few syntactical rules to distinguish regular function applications from constructor applications and the application of primitive functions.
A primitive is identified by a trailing `#` character in its name.
Supported primitive functions are `+#` (addition), `-#` (subtraction), `*#` (multiplication) and `/#` (division).
Functions and constructors are distinguished by the first character of their identifiers.
A constructor may only every start with an upper case letter, while lower case letters or symbols are allowed for variables describing a function.

The list of arguments passed to a function may only include atoms.
That is, only variables or primitive constants like (3#) can be used as an argument.

### Let Expressions

To bind more complex expressions to variables, so that they can be passed as an argument to a function, let expressions are required.
They act like a top-level binding, binding a lambda form to a name.
If the different names defined should be visible in other lambda forms bound by the same expression, a recursive let binding has to be used.

```
letrec one     = \ { } -> Int (1#)
       plusOne = \ { one } x -> +(one x)
in ...
```

This makes clear, why the *infer-free* extension might be useful for larger programs.
Additionally, the *expression-as-lambda* extension can be enabled using `-e expression-as-lambda` to allow an expression instead of a lambda form to be bound.

```
letrec one     = Int (1#) // Expression instead of a lambda form.
       plusOne = \ x -> +(one x) // Free variables are inferred.
in ...
```

### Case Expressions

The only way to enforce evaluation in the STG-language is by using case expressions.
They are defined using the `case` keyword followed by an arbitrary expression to evaluate.
Following this expression is a pair of curly parentheses, which define a sequence of patterns, that are used to match the evaluated expression.

```
case Expression {
    of Cons(head tail) -> Body1
    of Nil -> Body2
    default -> Body3
}
```

Cases that define a value or pattern start with the `of` keyword.
Default cases are either defined using the `default` keyword or by using a single identifier, to which a value is bound.

As with constructor applications, an empty pair of parenthesis can be omitted when defining a pattern for a case.


## References

The main reference for this project is "Implementing Lazy Functional Languages on Stock Hardware – The Spineless Tagless G-machine", by Simon Peyton Jones in the *Journal of Functional Programming* (1992).
An updated version of the STG, that more resembles the one implemented in the [Haskell Compiler](https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/compiler/generated-code) is described in "How to Make a Fast Curry – Push/Enter vs Eval/Apply", by Simon Peyton Jones in the *International Conference on Functional Programming* (2004).
This paper proposes optimizations regarding argument passing in higher-order functions, which are not implemented in this project.

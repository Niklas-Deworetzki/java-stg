# STG Machine

> The STG machine is an essential part of GHC, the world's leading
> Haskell compiler. It defines how the Haskell evaluation model
> should be efficiently implemented on standard hardware.
>
> https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/compiler/generated-code



```
              Desugar       STGify       CodeGen    Native CodeGen
 +------------+  |   +------+  |   +-----+  |   +-----+  |   +----------+
 | Parse tree |----->| Core |----->| STG |----->| C-- |----->| Assembly |
 +------------+      +------+      +-----+      +-----+      +----------+
```



## What can Haskell do?



```haskell
-- Arithmetic
3 + 2 * 2

-- Recursion
fac :: Int -> Int
fac 0 = 1
fac n = n * fac(n - 1)

fac 5

-- Higher order functions
map (fac) [1 .. 10]
```



## What else is there?



```java
int a() { 
    return 10 / 0; 
}

int b() { 
    return 20 / 0; 
}

int c(int a, int b) { 
    return 42; 
}

// What will happen?
c(a(), b()) 
```





```scala
def a: Int = 10 / 0
def b: Int = 20 / 0
def c(a: Int, b: Int): Int = 42

c(a, b)
```





```haskell
a :: Int
a = 10 `div` 0

b :: Int
b = 20 `div` 0

c :: (Int, Int) -> Int
c (a, b) = 42

c (a, b)
```

































## Exploring Laziness

```haskell
fibs :: [Integer]
fibs = 1 : 1 : zipWith (+) fibs (tail fibs)

gen :: Int -> [Int]
gen n = take n (repeat 0)

gen 100

foldr (&&) True ([True, False] ++ repeat True)
```





## A look at Scala's lazy val

Ein `lazy val` wird einmalig ausgewertet, wenn er das erste Mal verwendet wird.

Anschließend greift jede weitere Verwendung auf den zuvor ausgewerteten Wert zu.

https://docs.scala-lang.org/sips/improved-lazy-val-initialization.html



```scala

class Example {
    lazy val foo: Int = 42
}

```





















```scala

class Example {
    // Generated fields.
    var bitmap_0: Boolean = false
    var foo_0: Int = _

    // Evaluation function.
    private def foo_lzycompute(): Int = {
        if (!bitmap_0) {
            foo_0 = 42
            bitmap_0 = true
        }
        return foo_0
    }

    // Generated accessor.
    def foo: Int = if (bitmap_0) foo_0 else foo_lzycompute()
}

```



















```scala

class Example {
    // @volatile to make changes explicit in multithreading environment.
    @volatile var bitmap_0: Boolean = false
    var foo_0: Int = _

    private def foo_lzycompute(): Int = {
        // Lock the monitor of this instance.
        this.synchronized {
            if (!bitmap_0) {
                foo_0 = 42
                bitmap_0 = true
            }
        }
        return foo_0
    }

    def foo: Int = if (bitmap_0) foo_0 else foo_lzycompute()
}

```

















## In Haskell? - STG Machine!

* **Spineless** – Die *Spine* beschreibt die Datenstruktur, welche einzelne Knoten hält (Bsp. verkettete Liste). Es existiert keine explizite Datenstruktur des Graphen im Speicher.
* **Tagless** – Tags werden verwendet, um Werte mit einem Label zu markieren. Ausgewertet/Nicht ausgewertet oder welcher Konstruktor vorliegt.
* **Graph-Reduction** – Programm ist ein Graph. Reduktion des Graphen entspricht der Auswertung des Programms.
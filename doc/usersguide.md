# skred users guide

## The Core Language



### Case Expressions

Case expressions combine dispatching on the tag of a data value and unpacking its fields.
The syntax of case expressions is:

> case := case *expression* of { *alt* ( ; *alt* )* }
>
> alt := <*tag*> *id** -> *expression*
>     |  *id* -> *expression*

For example:

> length xs = case xs of {
>               <0> -> 0;
>               <1> _ tl -> 1 + length tl
>             }

Case expressions which contain a tag *n* must also contain all tags less than *n*.
In other words, they must contain all tags starting from 0 up to the largest you
want to handle, even if they contain a default case. The following case expression
is invalid and won't compile:

> badLength xs = case xs of {
>                  <1> _ tl -> 1 + badLength tl;
>                  _ -> 0;
>                }

Case expression compile to **Case** primitives. Case primitives are parametrized
by the arities of the alternatives. There are three variants: without default
case, with default case using the expression value, and with default case
ignoring the expression value.

As an example, the length function defined above would compile to:

> Case{0,2} 0 (\\_ tl -> 1 + length tl) xs

A case expression with default

> case foo of { <0> x y -> bar x y; x -> quux x }

compiles to

> Case{2,*} (\\x y -> bar x y) (\\x -> quux x) foo

A case expression with a default that ignores the value

> case foo of { <0> x y -> bar x y; _ -> quux foo }

compiles to

> Case{2,_} (\\x y -> bar x y) (quux foo) foo

### Conditionals (if)

An if is syntactic sugar for the equivalent case expression:

> if foo then bar else quux

is the same as

> case foo of { <0> -> quux; _ -> bar }

This interprets data objects with tag 0 as false and all others as true.

## List of Built-in functions

### Basic combinators

Function | Reduction rule | Remark
-------- | -------------- | ------
S    | S f g x -> f x (g x)
K    | K c x -> c                           | Haskell: const
I    | I x -> x                             | Haskell: id
B    | B f g x -> f (g x)                   | Haskell: (.)
C    | C f x y -> f y x                     | Haskell: flip

### Numeric Functions

Function | Reduction rule | Remark
-------- | -------------- | ------
add      | add m n -> m + n   | Integer addition
sub      | sub m n -> m - n   | Integer subtraction
mul      | mul m n -> m * n   | Integer multiplication
Rsub     | Rsub m n -> n - m  | flip sub


### Structured Data

Function | Reduction rule | Remark
-------- | -------------- | ------
**Pack{*t*,*a*}** | **Pack{*t*,*a*}** x0 x1 ... -> Data{*t*,x0,x1,...} | Constructor
**Case{*a0*,*a1*,...}** | See section "Case Expressions"



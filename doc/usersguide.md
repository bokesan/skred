# skred user's guide

*skred* implements a very simple lazy functional language called the *core language*
using combinator graph reduction.

## Running programs

    $ cat hello.core
    main = putStr "Hello, World\n"
    $ java -jar skred.jar hello.core
    Hello, World

### Command options

Option | Description
------ | -----------
--evalprojections --noevalprojections | evaluate result of projection functions?
--app=Cond\|IndI\|ST  |  choose implementation of overwriting.
--stats=_file_ | report evaluation statistics in _file_.
--noopt        | don't do optimizations like `C add ==> add`.
--useB1        | use B' instead of B* combinator
--no-prelude   | don't load prelude.core


## The Core Language

The core language is very similar to the one used in
[Implementing functional languages](http://research.microsoft.com/en-us/um/people/simonpj/Papers/pj-lester-book/).

There are a few differences to make it more similar to Haskell, for
example the use of braces in `let` and `case` expressions
and using `/=` instead of `~=` for inequality.

There is also a bit more syntactic sugar.

See the [prelude](../lib/prelude.core) and the [test](../test) folder for some
example core programs.

### Data Types

The core language has only few data types:

* Integers
* Tagged structures
* Functions

Characters a represented by integers, everything else by structured data.

A data item has a non-negative _tag_, and 0 or more fields. Fields are indexed
from 0. A data item with tag _t_ and _n_ fields is represented as
<code>Data{<i>t</i>,x<sub>0</sub>,…,x<sub>n-1</sub>}</code>.


### Syntax

A *program* is a list of definitions separated by semicolons.

> program  =  defn { **;** defn }
> defn     =  dname { pvar } **=** expression
    dname       ::=  conid | varid
    pvar        ::=  varid | _
    conid       ::=  <uppercase letter> 

#### Infix operators

Some infix operators are provided as syntactic sugar for built-in functions.
In descending order of precedence:

| Operator | Function                | Associativity
| -------- | ----------------------- | -------------
| .        | B                       | right
| * / %    | mul quot rem            | left
| + -      | add sub                 | left
| : ++     | Pack{1,2} append        | right
| = /= < <= > >= | eq ne lt le gt ge | non
| &&       | and                     | right
| \|\|     | or                      | right
| $        | function application    | right


### Case Expressions

Case expressions combine dispatching on the tag of a data value and unpacking its fields.
The syntax of case expressions is:

> case  = **case** expression **of {** alt { **;** alt } **}**
>
> alt   = **<** tag **>** { id } **->** expression
>       | id **->** expression

For example:

      length xs = case xs of {
                    <0> -> 0;
                    <1> _ tl -> 1 + length tl
                  }

Case expressions which contain a tag *n* must also contain all tags less than *n*.
In other words, they must contain all tags starting from 0 up to the largest you
want to handle, even if they contain a default case. The following case expression
is invalid and won't compile:

      badLength xs = case xs of {
                       <1> _ tl -> 1 + badLength tl;
                       _ -> 0;
                     }

Case expressions compile to **Case** primitives. Case primitives are parametrized
by the arities of the alternatives. There are three variants: without a default
case, with a default case using the expression value, and with a default case
ignoring the expression value.

As an example, the length function defined above would compile to:

      Case{0,2} 0 (\_ tl -> 1 + length tl) xs

A case expression with default

      case foo of { <0> x y -> bar x y; x -> quux x }

compiles to

      Case{2,*} (\x y -> bar x y) (\x -> quux x) foo

A case expression with a default that ignores the value

      case foo of { <0> x y -> bar x y; _ -> quux foo }

compiles to

      Case{2,_} (\x y -> bar x y) (quux foo) foo

### Conditionals (if)

`if` is syntactic sugar for the equivalent case expression:

      if foo then bar else quux

is the same as

      case foo of { <0> -> quux; _ -> bar }

This interprets data objects with tag 0 as false and all others as true.

## List of Built-in functions

### Basic combinators

Function | Reduction rule | Remark
-------- | -------------- | ------
S    | S f g x -> f x (g x)
K    | K c x -> c                           | Haskell: const
I    | I x -> x                             | Haskell: id
B    | B f g x -> f (g x)                   | Haskell: (.)
C    | C f x y -> f y x                     | Haskell: 
B'   | B' p q r s -> p q (r s)
Bs   | Bs p q r s -> p (q (r s))            | Sheevel's B* 
C'   | C' p q r s -> p (q s) r
S'   | S' p q r s -> p (q s) (r s)
J    | J p q r -> p q
J'   | J' p q r s -> p q r
W    | W f x -> f x x
Y    | Y f -> f (Y f)                       | builds cyclic graph


### Numeric Functions

Function | Reduction rule | Remark
-------- | -------------- | ------
add      | add m n -> m + n   | Integer addition
sub      | sub m n -> m - n   | Integer subtraction
mul      | mul m n -> m * n   | Integer multiplication
quot     | Integer quotient
rem      | Integer remainder
Rsub     | Rsub m n -> n - m  | flip sub
Rquot    | flip quot
Rrem     | flip rem
succ     | succ n -> n + 1
pred     | pred n -> n - 1
compare  | compare m n \| m < n -> Data{0} \| m = n -> Data{1} \| m > n -> Data{2}
eq       | eq m n \| m = n -> Data{1} \| m ≠ n -> Data{0}
ne       | ne m n \| m ≠ n -> Data{1} \| m = n -> Data{0}
lt       | lt m n \| m < n -> Data{1} \| m ≥ n -> Data{0}
le       | le m n \| m ≤ n -> Data{1} \| m > n -> Data{0}
gt       | gt m n \| m > n -> Data{1} \| m ≤ n -> Data{0}
ge       | ge m n \| m ≥ n -> Data{1} \| m < n -> Data{0}
zero     | zero n \| n = 0 -> Data{1} \| m ≠ 0 -> Data{0}

### IO and System

Function | Reduction rule | Remark
-------- | -------------- | ------
error    | *terminate program*
primStdPort | TODO
hPutChar | hPutChar h c w -> Data{0,Data{0},w'} \| ioError
hGetChar | hGetChar h w -> Data{0,c,w'} \| ioError
_READ    | TODO
cmdLine  | -              | List of command line arguments

### Structured Data

Function | Reduction rule | Remark
-------- | -------------- | ------
Pack{*t*,*n*} | Pack{*t*,*n*} x<sub>0</sub> … x<sub>n-1</sub> -> Data{*t*,x<sub>0</sub>,…,x<sub>n-1</sub>} | Constructor
Case{*a0*,*a1*,...} | See section "Case Expressions"
Unpack{_n_} | Unpack{_n_} f Data{_t_,x<sub>0</sub>,…} -> f x<sub>0</sub> … x<sub>n-1</sub>
Get{_n_} | Get{_n_} Data{_t_,x<sub>0</sub>,…} -> x<sub>n</sub> | fst = get{0}; snd = get{1}
tag      | tag Data{_t_,…} -> t \| tag _ -> -1   | tag; -1 for integers
fields   | fields Data{_t_,x<sub>0</sub>,…,x<sub>n-1</sub>} -> n \| arity _ -> -1  | number of fields; -1 for integers

The functions _tag_ and _fields_ can be used for type predicates:

      isNumber obj = tag x < 0
      isPair obj   = and (tag x == 1) (fields x == 2)

Note that the latter definition could not be done with case, like this:

      isPair2 obj  = case obj of {
                       <0> -> False;
                       <1> x y -> True
                     }

This would raise an error if applied to a number or to a data item with tag 1
but less the two fields, and it would return True for a data item with
tag 1 and more than two fields.

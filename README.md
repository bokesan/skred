# SKRED [![Build Status](https://travis-ci.org/bokesan/skred.svg?branch=master)](https://travis-ci.org/bokesan/skred)

An SK combinator graph reduction engine implemented in Java.

Features:

* A simple core language.
* Built-in functions for arithmetic and structured data.
* Can be used standalone or as the target language for a more sophisticated compiler.
* Reasonable performance (for SK combinator reduction, that is).

## Documentation

See [the documentation](doc/usersguide.md). The [test](test) folder contains some sample programs.

## Installing

Run "ant jar" and move dist/skred.jar to a location of your choice.

## Running core programs

> java -jar skred.jar *your_program*.core

## License

skred is open source. It is distributed under the [MIT License](LICENSE).


# Tantilla

## TLDR

Main goal: Provide a reasonable way to code on a mobile phone (including a simple IDE).

Language: Roughly based on a Python subset (hence the name). From a language design perspective, 
it should always be possible to export working Python code, but this feature isn't implemented yet.

Notable differences:

 - Strict type system stealing traits from Rust
 - Explicit variable declarations required (var / const)
 - Blocks terminated by an explict "end" statement (enabling conditions and loops on the command line)
 - Lots of stuff missing

Code examples:

https://github.com/stefanhaustein/tantilla/tree/master/app/src/main/assets/examples

Android Play Store Beta link:

https://play.google.com/store/apps/details?id=org.kobjects.asde

## Properties and invocations

One special feature of Tantilla is that it parameterless functions and methods are invoked implicitly. 
Also, assignments to properties are implicity translated to `set_<name>` if there is no such property. 

This makes conversion between properties and setters / getters very simple, at the expense of having a
clear distinction between method calls and method references, adding the requirement of a special 
syntax for function and method references. 

## Traits instead of Class inheritance

While Tantilla supports classes, there is no support for inheritance between classes. Instead,
Traits and Trait implementations ("borrowed" from Rust) can be used for the same purpose. 

## Standard Library

Apart from some buit-ins borrowed from Python, there is currently only a simple graphics library and 
an even simpler sound library.

The sound library is just a `play` function that plays a string in ABC notation, including some 
limited emoji support for sound effects (at the moment just explosions and shots).

The graphics library consists of a singleton screen object where sprites and text objects 
can be added, based on the "krash" Android library specifically created for this purpose.

The plan is to move it towards a simple retained mode vector graphics library somewhere in
the area of Flash and SVG.

## Open Issues

### Concurrency

Here, I am leaning towards implementing async/await, again with most details borrowed from Rust.

### Immutability

This is my biggest pain point: How can I limit mutability in a seamless convenient and safe way?

### Library support

The foundations should be there, but some practical aspects are missing. Should there be a 
standard repository? How is it managed? How is it possible to ensure safety for an app rated
for kids in the app store?

### Standard Library Development

Currently, the graphics library is a strange mix of bitmap and vector API. I am planning to simplify
this to vector-only.

### Generics

Mostly hoping that avoiding traditional OO-inheritence avoids most pain points. Go has done well without so far,
so this is not a primary priority at the moment. 

### Direction

The biggest open issue is probably direction: what matters for the main purpose of the language
(mobile programming and a stepping stone from Scratch etal. to Python and/or Rust?








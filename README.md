# Introduction

Jazz is a Java2D toolkit based from HCIL of University of Maryland.
This repository was created from tarballs downloaded from <http://www.cs.umd.edu/hcil/piccolo/>.
Jazz is no longer actively maintained by its original authors and
user should migrate to [Piccolo2D](http://piccolo2d.org).

The main goal of this repository is to make Jazz building with up to date
version of Java.

# Getting started

Jazz comes with seven jar files:

|jar name| description|
|--------------|--------------------------------------------------|
| jazz.jar     | This jar contains the Jazz 2d graphics framework.|
| jazzx.jar    | This jar contains nonessential, but mabye usefull jazz framework code.|
| hinote.jar   | This jar contains Hinote, a zoomable drawing program with hyperlinks.|
| help.jar     | This jar is used by the Hinote application.|
| graphit.jar  | This jar contains Graphit, a simple jazz graph drawing program.|
| examples.jar | This jar contains simple examples of the jazz programs.|
| tests.jar    | This jar contains unit tests for classes in the jazz framework.|

These jar files (excluding jazz.jar which is a library) can all be run by
double clicking with the mouse on the jar file or by running the command

    java -jar <jar file name>

# More information

More Jazz documentation can be found in the ./doc directory of this release
and on the [Jazz web site](http://www.cs.umd.edu/hcil/jazz/).

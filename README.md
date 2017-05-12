# Evolutionary composition of images using a Genetic Algorithm

Based
on:
[Genetic Programming: Evolution of Monalisa - Roger Johansson](https://rogerjohansson.blog/2008/12/07/genetic-programming-evolution-of-mona-lisa/),
and [this javascript implementation](http://alteredqualia.com/visualization/evolve/)

A Genetic Algorithm that uses a simulated annealing-like heuristic to "evolve" a
representation of a given image using a finite set of polygons, while
leveraging [clojure.spec](https://clojure.org/guides/spec) to generate random
data.

 - [Slides for talk at clojure.berlin - May 10th, 2017](http://volrath.me/talks/clj-evo-images)

## Usage

Run it with boot:

    $ boot wait run

Or standalone - build an uberjar from the project:

    $ boot build
    $ java -jar target/evo-images-0.1.0-SNAPSHOT-standalone.jar [args]

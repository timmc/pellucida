# pellucida

A web gallery fed by kpawebgen.

## Usage

With Leiningen v2:

```clojure
PELL_CONFIG=path/to/config.clj lein ring server
```

The config file must be a Clojure map of two keys:

* :thumbs-base -- path to directory of image files
* :gallery-db -- path to SQLite v3 DB

## License

Copyright © 2012 Tim McCormack

Distributed under the Eclipse Public License, the same as Clojure.

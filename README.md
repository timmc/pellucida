# pellucida

A web gallery fed by kpawebgen, written in Clojure.

## Usage

With Leiningen v2:

```clojure
PELL_CONFIG=path/to/config.clj lein run
```

Configuration is in Clojure, and is expected to look something like this:

```clojure
{:thumbs-link-base "/proxy-image/" ;; Proxy files from the local filesystem
 :thumbs-proxy-base "/home/timmc/photos/web/gen/"
 :gallery-db "/home/timmc/photos/web/kpawebgen.db3"
 ;; Optional keys
 :gmaps-api-key "AB..." ;; Google Maps v2 API key
 :btc-donate-addr "1..."
 :dev true
 :port 8080}
```

Requires a kpawebgen gallery DB version 1.

Keys are documented in `org.timmc.pellucida.settings`.

## License

Copyright © 2012–2014 Tim McCormack, except for vendored items in
`./resources/public/vendor`.

Distributed under the Eclipse Public License v1.0, the same as Clojure.

Division 2.0.0 CSS grid system is available under the Creative Commons
Attribution-ShareAlike 3.0 Unported License.

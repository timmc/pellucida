# pellucida

A photo gallery website fed by [kpawebgen](https://github.com/timmc/kpawebgen),
written in Clojure.

## Demo

Currently (April 2016) deployed as http://gallery.brainonfire.net/ on
[NearlyFreeSpeech.net](https://www.nearlyfreespeech.net/). I developed
it on Heroku as pellucida.herokuapp.com, so you may see some
references to that in the codebase and commit history.

## Usage

With Leiningen v2:

```clojure
PELL_CONFIG=path/to/config.clj lein run
```

Configuration is in Clojure, and is expected to look something like this:

```clojure
{:thumbs-link-base "/v2/proxy-image/" ;; Proxy files from the local filesystem
 :thumbs-proxy-base "/home/timmc/photos/web/gen/"
 :gallery-db "/home/timmc/photos/web/kpawebgen.db3"
 ;; Optional keys
 :google-static-maps-v2-api-key-browser "AI..." ;; Google Maps v2 API key
 :btc-donate-addr "1..."
 :port 8080}
```

Requires a kpawebgen gallery DB version 1.

Keys are documented in `org.timmc.pellucida.settings`.

`PELL_DEV=true` enables automatic code reloading.

## TODO

- Bugfixes:
    - Geocode maps no longer link to a map that labels the marker, now
      that Google Maps has been updated.
    - Gallery layout shows uneven rows of images -- switch to multiple
      of column count
    - Tag `Content:back yard` not showing up -- confirm it is even
      being included
- Show mode switcher (or remover) now that modes are supported
  (involves some plumbing work)
- Atom feed
- Better titles, especially on filtered list view
- Sort by creation chronology, image ID, or newest present in gallery
  (last may not be possible, but is most interesting for a feed)
- Add Drawbridge REPL (but nREPL needs a secure way to pass auth!)

## License

Copyright © 2012–2016 Tim McCormack, except for vendored items in
`./resources/public/vendor`.

Distributed under the Eclipse Public License v1.0, the same as Clojure.

Division 2.0.0 CSS grid system is available under the Creative Commons
Attribution-ShareAlike 3.0 Unported License.

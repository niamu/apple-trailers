# apple-trailers

Download [movie trailers from Apple](http://trailers.apple.com).

## Usage

Require the library...

```Clojure
user> (require '[apple-trailers.core :as trailers])
```

Provide a specific trailer URL to download from...

```Clojure
user> (-> (trailers/trailers "http://trailers.apple.com/trailers/independent/swissarmyman/")
          trailers/download-all)
```

You can also perform a search for the movie. This will fetch the trailers from the top result of the search so it may not always be what you expect.

```Clojure
user> (-> (trailers/search "Swiss Army Man")
          trailers/download-all)
```

`download-all` will find all trailers for the movie and download them in the highest quality available.

## License

Copyright © 2016 Brendon Walsh

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

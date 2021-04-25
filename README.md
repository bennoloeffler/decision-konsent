# decision-konsent

## Running

set database env for prod heroku: `$env:DATABASE_URL="postgresql://localhost/konsent_heroku?user=postgres&password=xr600r"`  

run locally: `lein run`  
  
compile permenently: `lein shadow watch app`  

test heroku deploy: `heroku local`

now with buildpack nodejs and clojure

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen


To start a web server for the application, run:

    lein run 

## License

Copyright © 2021 FIXME

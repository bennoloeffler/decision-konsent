# decision-konsent

## checkout, checkin, running, testing, deploy

create project: `lein new luminus decision-konsent +cljs +swagger +postgres +reagent +re-rame +shadow-cljs`   
set database env for prod/env locally: `$env:DATABASE_URL="postgresql://localhost/konsent_heroku?user=postgres&password=xr600r"`  
run locally: `lein run`  
compile permenently: `lein shadow watch app`  
test heroku deploy: `heroku local`  
continuous test: `lein test-refresh`  
heroku with buildpack nodejs and clojure  
scss/sass build and watch: `npm run css-watch` and css-build (see package.json)  

## user stories
1. datastructure for a growing consent, that is used and notified between clients and server
1. backend-server with database that creates and saves konsents (save-consent)
1. register as user (if you want to create konsent)
1. login as user (email, password)
1. validation of forms (client and server)
1. create a consent (invite participants while creation)
1. see all my running kosents 
1. invite participants after creation
1. use an invitation link to take part in an konsent without registration
1. disuss (write a message into a running konsent)
1. start voting (and see, who has votet - but not how)
1. finish voting (even if sombody did not vote)
1. notify invitation per email
1. emoties in discussions
1. emoties vor voting

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen


To start a web server for the application, run:

    lein run 

## License

Copyright © 2021 FIXME

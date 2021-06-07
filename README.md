# decision-konsent

## prepare
install npm: https://nodejs.org/en/download/
install dart-sass or node-sass --> npm install node-sass
install shadow 
install lein
## checkout, checkin, running, testing, deploy

run the build tools:
start console (3x)
cd \projects\_clj\decision-konsent

#### 1 SCSS (css rebuild + reload as typed)
npm run css-watch

#### 2 SHADOW (frontend, cljs - recompile + reload as typed)
lein shadow watch app

#### 3 TEST (run tests 
$env:DATABASE_URL="postgresql://localhost/konsent_test?user=postgres&password=xr600r"
lein test-refresh

#### 4 REPL (start, stop, restart - change and reload)
intellij

#### 5 Heroku
##### deploy
lein uberjar
heroku local
git commit -am "login register messages time"
git push heroku master
##### migrate
`heroku run bash | java -jar target/uberjar/decision-konsent.jar migrate`

create project: `lein new luminus decision-konsent +cljs +swagger +postgres +reagent +re-rame +shadow-cljs`   
set database env for prod/env locally: `$env:DATABASE_URL="postgresql://localhost/konsent_heroku?user=postgres&password=xr600r"`  
run locally: `lein run`  
compile cljs incrementally: `lein shadow watch app`  
build jar: `lein uberjar`  
test heroku deploy: `heroku local`  
continuous test: `lein test-refresh`  
heroku with buildpack nodejs and clojure  
scss/sass build and watch: `npm run css-watch` and css-build (see package.json)  
deploy to heroku: `git commit -am "xyz" | git push heroku master`
migrate and rollback: `heroku run bash | java -jar target/uberjar/decision-konsent.jar migrate`

## user stories
1. ok datastructure for a growing konsent, that is used and notified between clients and server
1. ok backend-server with database that creates and saves konsents (save-consent)
1. ok register as user (if you want to create konsent)
1. ok login as user (email, password)
1. validation of forms (client and server)
1. create a consent (invite participants while creation)
1. ok see all my running kosents 
1. invite participants after creation
1. use an invitation link to take part in an konsent without registration
1. ok disuss (write a message into a running konsent)
1. start voting (and see, who has voted - but not how)
1. ok finish voting (even if sombody did not vote)
1. notify invitation per email
1. emoties in discussions
1. emoties vor voting
1.  

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen


To start a web server for the application, run:

    lein run 

## License

Copyright © 2021 FIXME

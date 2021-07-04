# decision-konsent

## prepare
- install npm: https://nodejs.org/en/download/
- install shadow 
- install lein
- install sass dart-sass or node-sass --> npm install node-sass
- install postgresql (postgresql-13.2-2-windows-x64.exe)
- install pgadmin (pw x...r)

## checkout, checkin, running, testing, deploy

run the build tools:
- start console (3x)
- cd \projects\_clj\decision-konsent (your path...)

then:
#### 1 SCSS (css rebuild + reload as typed)
npm run css-watch

#### 2 SHADOW (frontend, cljs - recompile + reload as typed)
lein shadow watch app

#### 3 TEST (run tests 
$env:DATABASE_URL="postgresql://localhost/konsent_test?user=postgres&password=xr600r"  
or  
set DATABASE_URL="postgresql://localhost/konsent_test?user=postgres&password=xr600r"  
lein test-refresh

#### 4 REPL (start, stop, restart, reset-db)
in intellij:  
start repl (server)  
by right clicking on  
project.clj

#### 5 Heroku
##### deploy
lein uberjar
heroku local
git commit -am "login register messages time"
git push heroku master

##### migrate (or rollback)
`heroku run bash | java -jar target/uberjar/decision-konsent.jar migrate`


### all commands 

create project:  
`lein new luminus decision-konsent +cljs +swagger +postgres +reagent +re-rame +shadow-cljs`     
  
set database env for prod/env locally:  
`$env:DATABASE_URL="postgresql://localhost/konsent_heroku?user=postgres&password=xr600r"`  

run server locally:  
`lein run`  

compile cljs incrementally:  
lein shadow watch app`  

build jar:  
`lein uberjar`    

test heroku deploy:  
`heroku local`    

continuous test:  
`lein test-refresh`    

heroku with buildpack nodejs and clojure      

scss/sass build and watch:  
`npm run css-watch` and css-build (see package.json)    

deploy to heroku:  
`git commit -am "xyz" | git push heroku master`  

migrate and rollback:  
`heroku run bash | java -jar target/uberjar/decision-konsent.jar migrate`  

## user stories
1. ok datastructure for a growing konsent, that is used and notified between clients and server
1. ok backend-server with database that creates and saves konsents (save-consent)
1. ok register as user (if you want to create konsent)
1. ok login as user (email, password)
1. ok create a consent 
1. ok see all my running kosents 
1. ok disuss (write a message into a running konsent)
1. ok start voting (and see, who has voted - but not how)
1. ok finish voting (even if sombody did not vote)
1. ok keep alive by ping
1. ok force https
1. ok update other clients per websocket or server sent events
1. ok show results of itermediate votings in read, if veto or in yellow, if major
1. ok show accepted proposal in green
1. ok Button for "force vote" and "force ready"
1. ok invite participants after creation
1. ok german translation
1. validation of email and password (and other forms)
1. show the potential proposers for everybody  
1. Confirm dialog for "delete"
1. List and current konsent in one page  

--> BETA 1

1. make backwards / forward buttons functional (list of k / current k in one page)
1. archive konsents - but show them below the "active list"
1. have an archive button in the active list. have an delete button in the archived list.
1. use an invitation link to take part in an konsent without registration
1. validate a users email-adress by clicking an email-link
1. make an change your password.
1. make an "forgot your password"
1. ask for cockies
1. have german law imprint  
1. UNDO (see functional tv)
1. notifications (changes indicators - in the sense of "waiting for you")
1. Profile-pictures (round)
1. make it like a chat application

## nice to have and later
1. change email adress


## technical issues / refactoring / improvements
1. roles (validated, authenticated, known-guest, anonymous)  
for different api functions (login, save-konsent, ...)   
1. tests of all routes
1. all data as subscription (especially the "active konsent")
1. remove error after 20 seconds
1. ok provide a websocket notify api that is easy to implement in other languages: json  
   ok {"id" 17}
1. provide spec for API (swagger)
1. provide spec for konsent data 
1. UUID as user id - email as parameter
1. make the invitations an own field in konsent (id konsent invitations)  
   that way, it would be save from beeing manipulated by clients.
1. sdfsdf
   
## email invitation and roles
:authenticated :invited :no-auth 

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen


To start a web server for the application, run:

    lein run 

## License

Copyright © 2021 FIXME

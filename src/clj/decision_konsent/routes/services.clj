(ns decision-konsent.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [decision-konsent.middleware.formats :as formats]
    [ring.util.http-response :refer :all]
    [clojure.java.io :as io]
    [decision-konsent.db.core :as db]
    [ring.util.http-response :as response]
    [decision-konsent.auth :as auth]
    [decision-konsent.unix-time :as ut]
    [spec-tools.data-spec :as ds]
    [decision-konsent.db.konsent :as k]
    [clojure.pprint :as pp :refer [pprint]]
    [puget.printer :refer [cprint]]
    [decision-konsent.email :as email])

  (:import (clojure.lang ExceptionInfo)))

;; make it readable as html
(def color-scheme {; syntax elements
                   :delimiter       [:bold :red]
                   :tag             [:red]

                   ; primitive values
                   :nil             [:bold :black]
                   :boolean         [:green]
                   :number          [:cyan]
                   :string          [:bold :magenta]
                   :character       [:bold :magenta]
                   :keyword         [:bold :orange]
                   :symbol          nil

                   ; special types
                   :function-symbol [:bold :blue]
                   :class-delimiter [:blue]
                   :class-name      [:bold :blue]})


(defn service-routes []
  ["/api"
   {:coercion   spec-coercion/coercion
    :muuntaja   formats/instance
    :swagger    {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 coercion/coerce-exceptions-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc  true
        :swagger {:info {:title       "the konsent-app-api"
                         :description "https://cljdoc.org/d/metosin/reitit"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url    "/api/swagger.json"
              :config {:validator-url nil}})}]]

   ["/user"
    {:swagger {:tags ["user"]}}

    ["/register"
     {:post {:summary    "create a new user"
             :parameters {:body {:email string? :password string? :confirm string?}}
             :responses  {200 {:body {:email string? :message string?}}
                          400 {:body {:message string?}}
                          409 {:body {:message string?}}}
             :handler    (fn [{{:keys [email password confirm]} :body-params}]
                           (println "going to register user: " email)
                           (if-not (= password confirm)
                             (response/bad-request {:message "Password and confirm do not match."})
                             (try
                               (auth/create-user! email password)
                               (println "registered user: " email)
                               (response/ok {:email email :message "User registered. Please login."})
                               (catch ExceptionInfo e
                                 (if (= (:decision-konsent/error-id (ex-data e))
                                        ::auth/duplicate-user)
                                   (response/conflict {:message "Failed. User already exists."})
                                   (throw e))))))}}]


    ["/login"
     {:post {:summary    "user authentictes with email and password. auth-type = :auth (EDN) or \"auth\" (json)"
             :parameters {:body {:email string? :password string?}}
             :responses  {200 {:body {:identity {:email string? :auth-type keyword?}}}
                          401 {:body {:message string?}}}
             :handler    (fn [{{:keys [email password]} :body-params session :session :as data}]
                           (println "\n\ngoing to login:\n")
                           (pprint data)
                           (if-some [user (auth/authenticate-user email password)]
                             (-> (response/ok {:identity (assoc user :auth-type :auth)})
                                 (assoc :session (assoc session :identity user)))
                             (response/unauthorized {:message "Incorrect login or password."})))}}]


    ["/logout"
     {:post {:summary "user will be logged out - session will be not available any more"
             :handler (fn [_] (println "logged out")
                        (-> (response/ok {:logged-out-identity "ghost"})
                            (assoc :session nil)))}}]
    ["/session"
     {:get {:summary    "renew the session data after refresh and across browser tabs"
            :parameters {:query {}}
            ;:responses  {200 {:body {:session {:identity {:email string?}}}}} ;:auth-type keyword?}}}}}
            :handler    (fn [{{:keys [identity]} :session :as data}]
                          ;(println "\n\nsession:\n")
                          ;(cprint data)
                          (response/ok {:session
                                        {:identity
                                         (not-empty
                                           (select-keys identity [:email :auth-type]))}}))}}]]
   ["/konsent"
    {:swagger {:tags ["konsent"]}}

    ["/test-ajax"
     {:get  {:summary    "test data receiving and replying per ajax"
             :parameters {:query {:some string?}}
             :responses  {200 {:body {:you-GET-me {:some string?}}}}
             :handler    (fn [data]
                           ;(cprint data)
                           ;(cprint (:params data))
                           (response/ok {:you-GET-me (:params data)}))}

      :post {:summary    "test data receiving and replying per ajax"
             :parameters {:body {:some string?}}
             :responses  {200 {:body {:you-POST-me {:some string?}}}}
             :handler    (fn [data]
                           (cprint data)
                           (cprint (-> data :body-params))
                           (response/ok {:you-POST-me (-> data :body-params)}))}}]

    ["/unix-time"
     {:get {:summary   "returns unix GMT milliseconds of the server"
            ;:parameters {}
            :responses {200 {:body {:server-time int?}}}
            :handler   (fn [_] #_(println "get current time") (ok {:server-time (+ 0000 (ut/current-time))}))}}]

    ["/ping"
     {:get {:summary   "just see the konsent app living..."
            :responses {200 {:body {:message string?}}}
            :handler   (constantly (ok {:message "pong"}))}}]

    ["/create-konsent"                                      ; TODO error on server - reply with meaningful, displayable error message, see login
     {:post {:summary "An owner (o) starts an konsent - with short-name problem-description and participants (p). An id will be created."
             #_:parameters #_{:body {;:iterations vector?
                                     :owner             string?
                                     ;:participants vector?
                                     :problem-statement string?
                                     :short-name        string?
                                     :timestamp         int?}}
             :handler (fn [data]
                        ;(cprint data)
                        ;(cprint (-> data :body-params))
                        (let [params  (-> data :body-params)
                              konsent (k/create-konsent! params)
                              konsent (email/create-invitations
                                        konsent
                                        (:server-name data)
                                        (:server-port data))]

                          (assert (= 1 (k/save-konsent! konsent)))
                          (println "\n\n/create-konsent ")
                          (clojure.pprint/pprint konsent)
                          (email/send-invitations! konsent)
                          (response/ok konsent)))}}]

    ; b.l@gmx.net (guest) http://localhost:3000/api/konsent/invitation/1119765859799043611894664431870268928/76
    ["/invitation/:invitation-id/:konsent-id"
     {:summary "fast login by (existing, server sent) invitation link. auth-type = :auth (EDN) or \"auth\" (json)\" if user exists :guest otherwise"
      :get     (fn [a] (let [konsent-id-str (-> a :path-params :konsent-id)
                             konsent-id     (Integer/valueOf konsent-id-str)
                             invitation-id  (keyword (-> a :path-params :invitation-id))
                             konsent        (db/get-konsent {:id konsent-id})
                             invitation     (-> konsent :konsent :invitations invitation-id)
                             real-user      (db/get-user {:email (:guest-email invitation)})]
                         (if invitation
                           (do
                             (println "/invitation:")
                             (pprint invitation)
                             (-> (response/found (str "/#/my-konsent/" konsent-id))
                                 (assoc-in [:session :identity] {:email (:guest-email invitation) :auth-type (if real-user :auth :guest)})))
                           (-> (response/unauthorized "<h1>ERROR: Did not find invitation. Sorry...</h1>")
                               (response/header "Content-Type" "text/html; charset=utf-8")))))}]



    ["/save-konsent"
     {:post {:summary "user saves a konsent"
             :handler (fn [data]
                        ;(println "data: " data)
                        (let [params (-> data :body-params)]
                          (println "\n\n/save-konsent (:body-params):")
                          (clojure.pprint/pprint params)
                          ;(println "\n\n/save-konsent (all DATA):")
                          ;(cprint data)

                          (response/ok {:message (k/save-konsent! params)})))}}]

    ["/delete-konsent"
     {:post {:summary "User delete a konsent"
             :handler (fn [data]
                        ;(println "data: " data)
                        (let [params (-> data :body-params)]
                          ;(println "params: " params)
                          (response/ok {:message (k/delete-konsent! params)})))}}]

    ["/all-konsents-for-user"
     {:get {:summary "A user askes for all konsents he is involved in."
            :handler (fn [data]
                       (let [params (:params data)
                             result (k/get-konsents-for-user params)]
                         (ok result)))}}]

    #_["/invitation/:invitation-id/:konsent-id"
       {:get {:summary "A guest uses his invitation"
              :handler (fn [data]
                         (let [params (:params data)]
                           (println params)
                           (ok "invitation accepted")))}}]

    #_["/discuss"
       {:get {:parameters {:query {:text string?}}
              :summary    "owner (o) and participants (p) may discuss a while and even change the problem-description while finding ideas and options for possible solutions."
              ;:handler (constantly (ok {:message "well, you know - i think..."}))
              :handler    (fn [{{{:keys [text]} :query} :parameters}]
                            {:status 200
                             :body   {:message (str "i totally disagree with: " text)}})}}]
    ["/message"
     {:get  {:summary    "just get all messages"
             :parameters {:query {}}
             ;:responses {200 {:body {:total pos-int?}}}
             :handler    (fn [_]
                           ;(println "deliver messages: " (db/get-messages))
                           {:status 200
                            :body   {:messages (db/get-messages)}})}

      :post {:summary    "just send a message to everybody"
             :parameters {:body {:message string?}}
             ;:responses {200 {:body {:messages seq?}}}
             :handler    (fn [data]
                           (let [message (-> data :body-params :message)]
                             ;(println "received message: " message)
                             ;(println "received message: " )
                             ;(println "timestamp: " (ut/current-time))
                             (db/create-message! {:message message :timestamp (ut/current-time)})
                             ;(println "wrote message: " message)
                             {:status 200
                              :body   {:messages (db/get-messages)}}))}}]

    ["/delete-message"
     {:post {:summary    "delete a message with id"
             :parameters {:body {:id int?}}
             :responses  {200 {:body {:messages seq}}}
             :handler    (fn [data]
                           ;(println "received message: " data)
                           (let [id (-> data :body-params :id)]
                             ;(println "received message: " data)
                             ;(println "received id: " id)
                             ;(println "timestamp: " (ut/current-time))
                             (println "deleted messages: " (db/delete-message! {:id id}) "  (:id " id ")")
                             ;(println "wrote message: " message)
                             {:status 200
                              :body   {:messages (db/get-messages)}}))}}]]]

  #_["/o-start-suggest-ask-vote-iteration"
     {:get {:summary "when the time is right, somebody may start the formal konsent process. She then is the new owner (o)"
            :handler (constantly (ok {:message "from now on - lets get formal! owner is now: "}))}}]

  #_["/p-ask-to-understand-suggestion"
     {:get {:summary "all the participants (p) may ask one or more questions in order to understand the suggested decision"
            :handler (constantly (ok {:message "ask to get answers - not to pre-vote."}))}}]

  #_["/o-answer"
     {:get {:summary "the owner (o) has to answer the questions. Sometimes 'I don??t know' will be the truth"
            :handler (constantly (ok {:message "understanding and answering may make the owner learn"}))}}]

  #_["/p-no-more-questions"
     {:get {:summary "after a while, the participants (p) will signal: I have no more questions."
            :handler (constantly (ok {:message "finished asking - waiting for vote"}))}}]

  #_["/o-ask-for-vote"
     {:get {:summary "the owner (o) summarises the proposed decision with the details/learnings from the questions and asks the participants (p) to vote"
            :handler (constantly (ok {:message "this is my proposal XYZ. please vote. show concerns, veto - or just a YES!"}))}}]

  #_["/p-vote"
     {:get {:summary "a participant (p) votes with :yes :major :minor :veto and a hint"
            :handler (constantly (ok {:message "you voted wisely"}))}}]

  #_["/o-end-konsent"
     {:get {:summary "there may be no decision - but the owner stops the process. Either because there is no more need or she just gives up because of repeated major concerns or vetos"
            :handler (constantly (ok {:message "understanding and answering may make the owner learn"}))}}]



  #_["/math"
     {:swagger {:tags ["math"]}}

     ["/plus"
      {:get  {:summary    "plus with spec query parameters"
              :parameters {:query {:x int?, :y int?}}
              :responses  {200 {:body {:total pos-int?}}}
              :handler    (fn [{{{:keys [x y]} :query} :parameters}]
                            {:status 200
                             :body   {:total (+ x y)}})}
       :post {:summary    "plus with spec body parameters"
              :parameters {:body {:x int?, :y int?}}
              :responses  {200 {:body {:total pos-int?}}}
              :handler    (fn [{{{:keys [x y]} :body} :parameters}]
                            {:status 200
                             :body   {:total (+ x y)}})}}]]

  #_["/files"
     {:swagger {:tags ["files"]}}

     ["/upload"
      {:post {:summary    "upload a file"
              :parameters {:multipart {:file multipart/temp-file-part}}
              :responses  {200 {:body {:name string?, :size int?}}}
              :handler    (fn [{{{:keys [file]} :multipart} :parameters}]
                            {:status 200
                             :body   {:name (:filename file)
                                      :size (:size file)}})}}]

     ["/download"
      {:get {:summary "downloads a file"
             :swagger {:produces ["image/png"]}
             :handler (fn [_]
                        {:status  200
                         :headers {"Content-Type" "image/png"}
                         :body    (-> "public/img/warning_clojure.png"
                                      (io/resource)
                                      (io/input-stream))})}}]])

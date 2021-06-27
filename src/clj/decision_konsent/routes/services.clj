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
    [clojure.pprint :as pp]
    [puget.printer :refer [cprint]])

  (:import (clojure.lang ExceptionInfo)))

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
     {:post {:summary   "create a new user"
             ;:parameters {:body {:email string? :password string? :confirm string?}}
             :responses {200 {:body {:email string? :message string?}}
                         400 {:body {:message string?}}
                         409 {:body {:message string?}}}
             :handler   (fn [{{:keys [email password confirm]} :body-params}]
                          ;(println "going to register user: " email)
                          (if-not (= password confirm)
                            (response/bad-request {:message "Password and confirm do not match."})
                            (try
                              (auth/create-user! email password)
                              (response/ok {:email email :message "User registered. Please login."})
                              (catch ExceptionInfo e
                                (if (= (:decision-konsent/error-id (ex-data e))
                                       ::auth/duplicate-user)
                                  (response/conflict {:message "Failed. User already exists."})
                                  (throw e))))))}}]


    ["/login"
     {:post {:summary    "user authentictes with email and password"
             :parameters {:body {:email string? :password string?}}
             :responses  {200 {:body {:identity {:email string?}}}
                          401 {:body {:message string?}}}
             :handler    (fn [{{:keys [email password]} :body-params session :session :as data}]
                           ;(println "\n\nlogin:\n")
                           ;(cprint data)
                           (if-some [user (auth/authenticate-user email password)]
                             (-> (response/ok {:identity user})
                                 (assoc :session (assoc session :identity user)))
                             (response/unauthorized {:message "Incorrect login or password."})))}}]


    ["/logout"
     {:post {:handler (fn [_] (println "logged out")
                        (-> (response/ok {:logged-out-identity "ghost"})
                            (assoc :session nil)))}}]
    ["/session"
     {:get {:parameters {:query {}}
            :handler    (fn [{{:keys [identity]} :session :as data}]
                          ;(println "\n\nsessions:\n")
                          ;(cprint data)
                          ; TODO fix missing middleware (problem started by changing undertow to http-kit
                          ; TODO find for http-kit;(wrap-session {:cookie-attrs {:http-only true}})
                          (response/ok {:session
                                        {:identity
                                         (not-empty
                                           (select-keys identity [:email]))}}))}}]]
   ["/konsent"
    {:swagger {:tags ["konsent"]}}

    ["/test-ajax"
     {:get {:summary "test data receiving and replying per ajax"
            :parameters {:query {:some string?}}
            :handler (fn [data]
                         ;(cprint data)
                         (cprint (:params data))
                         (response/ok (:params data)))}

      :post {:summary "test data receiving and replying per ajax"
             :parameters {:body-params {:some string?}}
             :handler (fn [data]
                        (cprint data)
                        (cprint (-> data :body-params))
                        (response/ok (-> data :body-params)))}}]

    ["/unix-time"
     {:get {:summary "returns unix GMT milliseconds of the server"
            :handler (fn [_] #_(println "get current time") (ok {:server-time (+ 0000 (ut/current-time))}))}}]

    ["/ping"
     {:get {:summary "just see the konsent app living..."
            :handler (constantly (ok {:message "pong"}))}}]

    ["/create-konsent"                                      ; TODO error on server - reply with meaningful, displayable error message, see login
     {:post {:summary "An owner (o) starts an konsent - with short-name problem-description and participants (p). An id will be created."
             ;:parameters {:body {:email string? :password string?}}
             :handler (fn [data]
                        ;(println "data: " data)
                        (let [params (-> data :body-params)]
                          (println "params: " params)
                          (response/ok (k/create-konsent! params))))}}]

    ["/save-konsent"
     {:post {:summary "user saves a konsent"
             :handler (fn [data]
                        ;(println "data: " data)
                        (let [params (-> data :body-params)]
                          ;(println "\n\nsaving konsent:")
                          ;(pp/pprint params)
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
                       (let [params  (:params data)
                             result (k/get-konsents-for-user params)]
                         (ok result)))}}]

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
             :parameters {:body-params {:message string?}}
             ;:responses {200 {:body {:messages seq?}}}
             :handler    (fn [data]
                           (let [message (-> data :body-params  :message)]
                             ;(println "received message: " message)
                             ;(println "received message: " )
                             ;(println "timestamp: " (ut/current-time))
                             (db/create-message! {:message message :timestamp (ut/current-time)})
                             ;(println "wrote message: " message)
                             {:status 200
                              :body   {:messages (db/get-messages)}}))}}]

    #_["/o-start-suggest-ask-vote-iteration"
       {:get {:summary "when the time is right, somebody may start the formal konsent process. She then is the new owner (o)"
              :handler (constantly (ok {:message "from now on - lets get formal! owner is now: "}))}}]

    #_["/p-ask-to-understand-suggestion"
       {:get {:summary "all the participants (p) may ask one or more questions in order to understand the suggested decision"
              :handler (constantly (ok {:message "ask to get answers - not to pre-vote."}))}}]

    #_["/o-answer"
       {:get {:summary "the owner (o) has to answer the questions. Sometimes 'I don´t know' will be the truth"
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
              :handler (constantly (ok {:message "understanding and answering may make the owner learn"}))}}]]



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
                                       (io/input-stream))})}}]]])

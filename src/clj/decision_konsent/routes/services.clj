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
    [clojure.java.io :as io]))

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
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
   ["" {:no-doc true
        :swagger {:info {:title "my-api"
                         :description "https://cljdoc.org/d/metosin/reitit"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]

   ["/konsent"
     {:swagger {:tags ["konsent"]}}


     ["/ping"
      {:get {:summary "just see the konsent app living..."
             :handler (constantly (ok {:message "pong"}))}}]

     ["/o-create-konsent"
      {:get {:summary "an owner (o) starts an konsent - with problem-description and participants (p)"
             :handler (constantly (ok {:message "owner created konsent"}))}}]

     ["/discuss"
      {:get {:parameters {:query {:text string?}}
             :summary "owner (o) and participants (p) may discuss a while and even change the problem-description while finding ideas and options for possible solutions."
             ;:handler (constantly (ok {:message "well, you know - i think..."}))
             :handler (fn [{{{:keys [text]} :query} :parameters}]
                        {:status 200
                         :body {:message (str "i totally disagree with: " text)}})}}]

     ["/o-start-suggest-ask-vote-iteration"
      {:get {:summary "when the time is right, somebody may start the formal konsent process. She then is the new owner (o)"
             :handler (constantly (ok {:message "from now on - lets get formal! owner is now: "}))}}]

     ["/p-ask-to-understand-suggestion"
      {:get {:summary "all the participants (p) may ask one or more questions in order to understand the suggested decision"
             :handler (constantly (ok {:message "ask to get answers - not to pre-vote."}))}}]

     ["/o-answer"
      {:get {:summary "the owner (o) has to answer the questions. Sometimes 'I don´t know' will be the truth"
             :handler (constantly (ok {:message "understanding and answering may make the owner learn"}))}}]

     ["/p-no-more-questions"
      {:get {:summary "after a while, the participants (p) will signal: I have no more questions."
             :handler (constantly (ok {:message "finished asking - waiting for vote"}))}}]

     ["/o-ask-for-vote"
      {:get {:summary "the owner (o) summarises the proposed decision with the details/learnings from the questions and asks the participants (p) to vote"
             :handler (constantly (ok {:message "this is my proposal XYZ. please vote. show concerns, veto - or just a YES!"}))}}]

     ["/p-vote"
      {:get {:summary "a participant (p) votes with :yes :major :minor :veto and a hint"
             :handler (constantly (ok {:message "you voted wisely"}))}}]

     ["/o-end-konsent"
      {:get {:summary "there may be no decision - but the owner stops the process. Either because there is no more need or she just gives up because of repeated major concerns or vetos"
             :handler (constantly (ok {:message "understanding and answering may make the owner learn"}))}}]]



   ["/math"
    {:swagger {:tags ["math"]}}

    ["/plus"
     {:get {:summary "plus with spec query parameters"
            :parameters {:query {:x int?, :y int?}}
            :responses {200 {:body {:total pos-int?}}}
            :handler (fn [{{{:keys [x y]} :query} :parameters}]
                       {:status 200
                        :body {:total (+ x y)}})}
      :post {:summary "plus with spec body parameters"
             :parameters {:body {:x int?, :y int?}}
             :responses {200 {:body {:total pos-int?}}}
             :handler (fn [{{{:keys [x y]} :body} :parameters}]
                        {:status 200
                         :body {:total (+ x y)}})}}]]

   ["/files"
    {:swagger {:tags ["files"]}}

    ["/upload"
     {:post {:summary "upload a file"
             :parameters {:multipart {:file multipart/temp-file-part}}
             :responses {200 {:body {:name string?, :size int?}}}
             :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                        {:status 200
                         :body {:name (:filename file)
                                :size (:size file)}})}}]

    ["/download"
     {:get {:summary "downloads a file"
            :swagger {:produces ["image/png"]}
            :handler (fn [_]
                       {:status 200
                        :headers {"Content-Type" "image/png"}
                        :body (-> "public/img/warning_clojure.png"
                                  (io/resource)
                                  (io/input-stream))})}}]]])

(ns decision-konsent.routes.home
  (:require
   [decision-konsent.layout :as layout]
   [decision-konsent.db.core :as db]
   [clojure.java.io :as io]
   [decision-konsent.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn home-page [request]
  (layout/render request "home.html"))

(defn add-rand-user! [request]
  (response/ok (db/add-rand-user!)))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/adduser" {:get add-rand-user!}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]])


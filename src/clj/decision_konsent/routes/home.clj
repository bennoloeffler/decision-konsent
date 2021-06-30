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

(defn get-users [request]
  (response/ok (db/get-all-users)))

(defn migrate! [request]
  (response/ok {:result (db/migrate!)}))

(defn rollback! [request]
  (response/ok {:result (db/rollback!)}))




(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   #_["/invitation/:invitation-id/:konsent-id" {:get (fn [a]  (-> (response/ok (with-out-str (puget.printer/cprint (:path-params a) {:color-markup :html-inline :color-scheme color-scheme})))
                                                                  (response/header "Content-Type" "text/html; charset=utf-8")))}]
   ["/adduser" {:get add-rand-user!}]
   ["/allusers" {:get get-users}]
   ["/migrate" {:get migrate!}] ; TODO find a better solution
   ["/rollback" {:get rollback!}] ; TODO find a better solution

   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]])


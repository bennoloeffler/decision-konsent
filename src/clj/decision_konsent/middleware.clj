(ns decision-konsent.middleware
  (:require
    [decision-konsent.env :refer [defaults]]
    [clojure.tools.logging :as log]
    [decision-konsent.layout :refer [error-page]]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
    [decision-konsent.middleware.formats :as formats]
    [muuntaja.middleware :refer [wrap-format wrap-params]]
    [decision-konsent.config :refer [env]]
    [ring.middleware.flash :refer [wrap-flash]]
    ;[ring.adapter.undertow.middleware.session :refer [wrap-session]]
    [ring.middleware.session :refer [wrap-session]]
    [ring.middleware.session.cookie :refer [cookie-store]]
    [ring-ttl-session.core :refer [ttl-memory-store]]
    [ring.middleware.defaults :refer [site-defaults secure-site-defaults wrap-defaults]]
    [ring.middleware.ssl :refer [wrap-hsts wrap-ssl-redirect wrap-forwarded-scheme]])
  (:import (javassist.bytecode ByteArray)))


(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))


(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title "Invalid anti-forgery token"})}))


(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))


(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)

      wrap-flash
      ;(wrap-session {:cookie-attrs {:http-only true}})
      (wrap-session {:store (cookie-store {:key (byte-array [12 2 45 32 34 3 4 6 7 8 32 98 36 23 56 12])})})
      (wrap-defaults
        (-> (:security-middleware defaults) ; site-defaults ; (assoc secure-site-defaults :proxy true)
            (assoc-in [:security :anti-forgery] false)))
            ;(assoc-in [:session :store] (ttl-memory-store (* 60 60 8)))))
            ;(dissoc :session)))
      wrap-internal-error))

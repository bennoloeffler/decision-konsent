(ns decision-konsent.prod-middleware
  (:require
    ;[ring.middleware.ssl :refer [wrap-reload]]
    ;[selmer.middleware :refer [wrap-error-page]]
    ;[prone.middleware :refer [wrap-exceptions]]
    [ring.middleware.ssl :refer [wrap-hsts]]))

(defn wrap-prod [handler]
  (-> handler
      ( wrap-hsts {:max-age 86400})))


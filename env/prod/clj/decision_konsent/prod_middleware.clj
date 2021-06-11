(ns decision-konsent.prod-middleware
  (:require
    ;[ring.middleware.ssl :refer [wrap-reload]]
    ;[selmer.middleware :refer [wrap-error-page]]
    ;[prone.middleware :refer [wrap-exceptions]]
    [ring.middleware.ssl :refer [wrap-hsts wrap-forwarded-scheme wrap-ssl-redirect]]))

(defn wrap-prod [handler]
  (-> handler
      wrap-forwarded-scheme
      wrap-ssl-redirect))
      ;(wrap-hsts {:max-age 86400})))

;(wrap-forwarded-scheme (wrap-ssl-redirect (wrap-hsts app)))
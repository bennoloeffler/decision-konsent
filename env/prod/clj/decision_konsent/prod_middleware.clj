(ns decision-konsent.prod-middleware
  (:require
    ;[ring.middleware.ssl :refer [wrap-reload]]
    ;[selmer.middleware :refer [wrap-error-page]]
    ;[prone.middleware :refer [wrap-exceptions]]
    [ring.middleware.ssl :refer [wrap-hsts wrap-forwarded-scheme wrap-ssl-redirect]]
    [puget.printer :refer [cprint]]))


(defn wrap-print-request [handler when]
 (fn [request]
   (println "\n\n --------- " when "----------")
   (clojure.pprint/pprint request)
   request))

(defn wrap-prod [handler]
      (-> handler
          (wrap-print-request "before: hsts")
          wrap-hsts
          (wrap-print-request "before: ssl-redirect")
          wrap-ssl-redirect
          (wrap-print-request "before: forwarded-scheme")
          wrap-forwarded-scheme
          (wrap-print-request "FINALLY")))

;(wrap-hsts {:max-age 86400})))

;(wrap-forwarded-scheme (wrap-ssl-redirect (wrap-hsts app)))
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
   (cprint request)
   request))

(defn wrap-prod [handler]
      (-> handler
          ;wrap-hsts
          (wrap-print-request "before ssl-redirect")
          wrap-ssl-redirect
          (wrap-print-request "before after ssl-redirect and before forwarded-scheme")
          wrap-forwarded-scheme))

;(wrap-hsts {:max-age 86400})))

;(wrap-forwarded-scheme (wrap-ssl-redirect (wrap-hsts app)))
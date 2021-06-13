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

;(assoc secure-site-defaults :proxy true)

(defn wrap-prod [handler]
      handler
      #_(-> handler
            ;(wrap-print-request "NO wrap")))
            ;(wrap-print-request "new before: hsts")))
            wrap-forwarded-scheme
            wrap-hsts
            ;(wrap-print-request "new before: ssl-redirect")
            wrap-ssl-redirect
            ;(wrap-print-request "new before: forwarded-scheme")

            (assoc secure-site-defaults :proxy true)))
;(wrap-print-request "FINALLY")))

;(wrap-hsts {:max-age 86400})))

;(wrap-forwarded-scheme (wrap-ssl-redirect (wrap-hsts app)))
(ns decision-konsent.dev-middleware
  (:require
    [ring.middleware.reload :refer [wrap-reload]]
    [selmer.middleware :refer [wrap-error-page]]
    [prone.middleware :refer [wrap-exceptions]]
    [ring.middleware.defaults :refer [site-defaults secure-site-defaults wrap-defaults]]))


(defn wrap-dev [handler]
  (-> handler
      wrap-reload
      wrap-error-page
      (wrap-exceptions {:app-namespaces ['decision-konsent]})
      #_(wrap-defaults
          (-> site-defaults
              (assoc-in [:security :anti-forgery] false)
              (dissoc :session)))))


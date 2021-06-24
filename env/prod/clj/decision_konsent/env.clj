(ns decision-konsent.env
  (:require [clojure.tools.logging :as log]
            [decision-konsent.prod-middleware :refer [wrap-prod]]
            [ring.middleware.defaults :refer [site-defaults secure-site-defaults wrap-defaults]]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[decision-konsent started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[decision-konsent has shut down successfully]=-"))
   :middleware identity
   :security-middleware (assoc secure-site-defaults :proxy true)})
    ;wrap-prod}) ;wrap-prod}) ;identity

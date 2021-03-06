(ns decision-konsent.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [decision-konsent.dev-middleware :refer [wrap-dev]]
    [ring.middleware.defaults :refer [site-defaults secure-site-defaults wrap-defaults]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[decision-konsent started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[decision-konsent has shut down successfully]=-"))
   :middleware wrap-dev
   :security-middleware site-defaults})


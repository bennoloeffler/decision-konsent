(ns decision-konsent.env
  (:require [clojure.tools.logging :as log]
            [decision-konsent.prod-middleware :refer [wrap-prod]]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[decision-konsent started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[decision-konsent has shut down successfully]=-"))
   :middleware wrap-prod}) ;wrap-prod}) ;identity

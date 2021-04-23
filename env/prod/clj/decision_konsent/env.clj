(ns decision-konsent.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[decision-konsent started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[decision-konsent has shut down successfully]=-"))
   :middleware identity})

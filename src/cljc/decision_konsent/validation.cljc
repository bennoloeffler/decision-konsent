(ns decision-konsent.validation
  (:require [struct.core :as st]))

(def message-schema
  [[:message
    st/required
    st/string
    {:message "message must contain at least one character"
     :validate (fn [msg] (>= (count msg) 0))}]])

(defn validate-message [params]
  (first (st/validate params message-schema)))
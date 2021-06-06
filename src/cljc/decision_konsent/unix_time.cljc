(ns decision-konsent.unix-time)

#?(:cljs
(defn current-time []
  (.getTime (js/Date.))))

#?(:clj
(defn current-time []
  (.getTime (java.util.Date.))))
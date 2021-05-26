(ns decision-konsent.unix-time)

#_(defn current-time []
  #?(:clj (.getTime (java.util.Date.)))
     :cljs (.getTime (js/Date.)))

(defn current-time []
  ;(println "Date-Server: " (java.util.Date.))
  (.getTime (java.util.Date.)))



(ns decision-konsent.routes.websocket
  (:require [clojure.tools.logging :as log]
            [org.httpkit.server :as http-kit]
            [clojure.edn :as edn]
            [clojure.data.json :as json]))


(defonce channels (atom #{}))


(defn connect! [channel]
  (log/info "Channel opened")
  (swap! channels conj channel))


(defn disconnect! [channel status]
  (log/info "Channel closed: " status)
  (swap! channels disj channel))

; TODO: create a queue and do async
(defn notify-all-clients! [data]
  (println "notify: " data)
  (doseq [channel @channels]
    (http-kit/send! channel (json/write-str data)))) ;TODO, remove the invalid channels


(defn handle-message! [channel ws-message]
  (let [message  (edn/read-string ws-message)
        response (try
                   (println "socket raw received: "  ws-message)
                   (println "socket read received: "  message)
                   message
                   ;(msg/save-message! message)
                   ;(assoc message :timestamp (java.util.Date.))
                   (catch Exception e
                     (let [{id     :guestbook/error-id
                            errors :errors} (ex-data e)]
                       (case id
                         :validation
                         {:errors errors}
                         ;;else
                         {:errors
                          {:server-error ["Failed to save message!"]}}))))]
    (if (:errors response)
      (http-kit/send! channel (pr-str response))
      (notify-all-clients! response))))


(defn handler [request]
  (http-kit/with-channel request channel
                         (connect! channel)
                         (http-kit/on-close channel (partial disconnect! channel))
                         (http-kit/on-receive channel (partial handle-message! channel))))


(defn websocket-routes []
  ["/ws"
   {:get handler}])
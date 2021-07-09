(ns decision-konsent.websocket
  (:require [cljs.reader :as edn]
            [re-frame.core :as rf]
            [clojure.string :as str]))
;(:import [java.lang Exception]))
;[clojure.data.json :as json]))


(defonce channel (atom nil))


(defn connect! [url receive-handler]
  (if-let [chan (js/WebSocket. url)]
    (do
      (set! (.-onmessage chan) #(as-> % message
                                      (.-data message)
                                      ;edn/read-string
                                      (.parse js/JSON message)
                                      (js->clj message :keywordize-keys true)
                                      (receive-handler message)))
      (reset! channel chan))
      ;(.send chan (clj->js {:init "websocket"}))
      ;(.log js/console "try to connect websocket"))
    (throw (ex-info "Websocket Connection Failed!"
                    {:url url}))))


(defn send-message! [msg]
  (let [chan @channel]
   (if (and chan (= 1 (.-readyState chan)))
    (do
      ;(println "readystate: " (.-readyState chan))
      (.send chan (.stringify js/JSON (clj->js msg))))                               ; TODO, try and handle: "WebSocket is already in CLOSING or CLOSED state."
    (throw (ex-info "Couldn't send message, channel isn't open!"
                    {:message msg})))))



(defn handle-response! [response]
  (if-let [errors (:errors response)]
    ;(rf/dispatch [:form/set-server-errors errors])
    (do
      (rf/dispatch [:common/set-error errors])
      (println "socket error: " (str errors)))
    (do
      (cond
        (:id response) (rf/dispatch [:konsent/load-list])
        (:keep-alive response) (do (println "received :randmon-keep-alive"))
                                   ;(send-message! {:random-keep-alive (rand-int 1000000)}))
        :default (.log js/console (str "socket received UNEXPECTED data: " response))))))


;(rf/dispatch [:message/add response])
;(rf/dispatch [:form/clear-fields response]))))


(defn init! []
  (let [host     (.-host js/location)
        ;_ (println host)
        protocol (if (str/starts-with? host "localhost") "ws" "wss")
        url-ws   (str protocol "://" host "/ws")]
    ;_        (println "url: " url-ws)]
    ;(.log js/console "try socket connection to: " url-ws)
    (connect! url-ws handle-response!)))


(ns decision-konsent.client-time
  (:require [re-frame.core :as rf]
            [ajax.core :refer [GET POST]]                   ; TODO
            [decision-konsent.ajax :as ajax]
            [decision-konsent.websocket :as ws]))


(defn current-time
  "unix time stamp in ms"
  []
  (.getTime (js/Date.)))


(rf/reg-event-db
  :set-server-diff-time
  (fn [db [_ match]]
    (assoc db :server-diff-time match)))


(defn get-server-time! []
  (let [start (current-time)]
    (GET
      "api/konsent/unix-time"
      {:handler       #(let [end               (current-time)
                             took              (- end start)
                             equivalent-client (- end (quot took 2))
                             server-time       (:server-time %)
                             diff-time         (- server-time equivalent-client)]
                         (rf/dispatch [:set-server-diff-time diff-time])
                         (if (> diff-time 3000)
                           (.log js/console (str "WARNING: server time seems very different [ms]: " diff-time))
                           (.log js/console (str "OK: Server-time and Browser-time synchronized."))))
       :error-handler #((rf/dispatch [:set-server-diff-time 0])
                        (.error js/console (str "ERROR getting server time. Setting diff to ZERO. Error: " %)))})))




(rf/reg-event-fx
  :init-server-diff-time
  (fn [_ _]
    (get-server-time!) {}))


(defn human-duration
  "human readable duration since someting occoured in the past - compared to now (unix time stamps [ms])"
  [past now]
  (let [sec        1000.0
        min        (* sec 60)
        h          (* 60 min)
        day        (* 24 h)
        week       (* 7 day)
        month      (* 30.4 day)
        year       (* 365 day)
        diff       (- now past)
        diff-sec   (/ (- now past) sec)
        diff-min   (/ (- now past) min)
        diff-h     (/ (- now past) h)
        diff-day   (/ (- now past) day)
        diff-week  (/ (- now past) week)
        diff-month (/ (- now past) month)
        diff-year  (/ (- now past) year)
        format-1   #(if (< % 2) (.toFixed % 1) (.toFixed % 0))]
    (cond
      (> sec diff) "now"                                    ; (format "%.3f" 2.0)
      (> min diff) (str (format-1 diff-sec) " seconds")
      (> h diff) (str (format-1 diff-min) " minutes")
      (> day diff) (str (format-1 diff-h) " hours")
      (> week diff) (str (format-1 diff-day) " days")
      (> month diff) (str (format-1 diff-week) " weeks")
      (> year diff) (str (format-1 diff-month) " months")
      (<= year diff) (str (format-1 diff-year) " years"))))


(defn human-duration-sd
  "Positive server-diff-time means: server is more ahead than client.
   Should be almost zero most of the time, since unix time stamp on server and on browser."
  ;without side effect for display with timer in re-frame
  ([past client-time server-diff-time]
   (human-duration (- past server-diff-time) client-time))
  ;with side effect
  #_([past server-diff-time]
     (human-duration-sd past (current-time) server-diff-time))
  ;with side effect - assuming, server-diff is near zero
  ([past client-time]
   (human-duration-sd past client-time 0))
  ([past]
   (human-duration-sd past (current-time) 0)))


;; -- Domino 1 - Event Dispatch -----------------------------------------------
;(def first-timer (atom true))

(defn dispatch-timer-event
  []
  (let [now (current-time)]
   ;(if first-timer
    ;(reset! first-timer false)
    (rf/dispatch [:timer now])))                            ;; <-- dispatch used

;; Call the dispatching function every xxx.
;; `defonce` is like `def` but it ensures only instance is ever
;; created in the face of hot-reloading of this file.
(defonce do-timer (js/setInterval dispatch-timer-event 10000))


;; -- Domino 2 - Event Handlers -----------------------------------------------

#_(rf/reg-event-db                                          ;; sets up initial application state
    :initialize                                             ;; usage:  (dispatch [:initialize])
    (fn [_ _]                                               ;; the two parameters are not important here, so use _
      {:time       (js/Date.)                               ;; What it returns becomes the new application state
       :time-color "#f88"}))                                ;; so the application state will initially be a map with two keys


#_(rf/reg-event-db                                          ;; usage:  (dispatch [:time-color-change 34562])
    :time-color-change                                      ;; dispatched when the user enters a new colour into the UI text field
    (fn [db [_ new-color-value]]                            ;; -db event handlers given 2 parameters:  current application state and event (a vector)
      (assoc db :time-color new-color-value)))              ;; compute and return the new application state


(rf/reg-event-fx                                            ;; usage:  (dispatch [:timer a-js-Date])
  :timer                                                    ;; every second an event of this kind will be dispatched
  (fn [{:keys [db]} [_ new-time]]                           ;; note how the 2nd parameter is destructured to obtain the data value
    {:db (assoc db :timestamp new-time)
     :dispatch [:ws-try-and-reconnect]}))                        ;; compute and return the new application state



(rf/reg-event-fx
  :ping
  (fn [_ _]
    (println "\n:ping")
    {:http-xhrio (ajax/wrap-get
                   {:uri        "/api/konsent/ping"
                    ;:params     nil
                    :on-success [:handle-ping]
                    :on-failure [:common/set-error]})}))

(rf/reg-event-fx
  :ws-try-and-reconnect
  (fn [_ _]
    ;(println "\n::ws-try-and-reconnect")
    (try
      (ws/send-message! {:random-keep-alive (rand-int 1000000)})
      (catch :default e (do #_(println "try reconnect due to ERROR: " e) (ws/init!))))))


(rf/reg-event-db
  :handle-ping
  (fn [_ [_ data]]
    (println "\n:handle-ping: " data)))


;; -- Domino 4 - Query  -------------------------------------------------------

(rf/reg-sub
  :timestamp
  (fn [db _]                                                ;; db is current app state. 2nd unused param is query vector
    (-> db
        :timestamp)))



(rf/reg-sub
  :server-diff-time
  (fn [db _]                                                ;; db is current app state. 2nd unused param is query vector
    (-> db
        :server-diff-time)))

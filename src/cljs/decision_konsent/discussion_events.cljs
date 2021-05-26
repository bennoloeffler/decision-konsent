(ns decision-konsent.discussion-events
  (:require [ajax.core :refer [GET POST]]
            [re-frame.core :as rf]
            [decision-konsent.ajax :as ajax]))

(rf/reg-event-fx
  :message/save
  (fn [{:keys [db]} [_ message]]
    (println "event: saving...")
    (ajax/send-message! message)
    {:db (assoc db :messages/loading? true)
     #_:ajax/post #_{:url           "/api/konsent/message"
                     :success-path  [:messages]
                     :success-event [:messages/set]}}))

(rf/reg-event-fx
  :messages/load
  (fn [{:keys [db]} _]
    (println "event: loading...")
    {:db       (assoc db :messages/loading? true)
     :ajax/get {:url           "/api/konsent/message"
                :success-path  [:messages]
                :success-event [:messages/set]}}))

(rf/reg-event-db
  :messages/set
  (fn [db [_ messages]]
    (println (str "event: set messages: " messages))
    (-> db
        (assoc :messages/loading? false
               :messages/list (reverse messages)))))

(rf/reg-sub
  :messages/loading?
  (fn [db _]
    (:messages/loading? db)))


(rf/reg-sub
  :messages/list
  (fn [db _]
    (:messages/list db)))



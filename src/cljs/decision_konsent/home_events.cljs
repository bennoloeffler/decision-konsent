(ns decision-konsent.home-events
  (:require
    [re-frame.core :as rf]
    [decision-konsent.ajax :as ajax]
    [decision-konsent.ajax :refer [wrap-post wrap-get]]))


(rf/reg-event-fx
  :message/save
  (fn [{:keys [db]} [_ message]]
    ;(println "event: saving..." message)
    ;(send-message! message)
    {:http-xhrio (wrap-post {:uri        "api/konsent/message"
                             :params     message
                             :on-success [:messages/set]
                             :on-failure [:common/set-error]})}))


(rf/reg-event-fx
  :messages/load
  (fn [{:keys [db]} _]
    ;(println "event: loading...")
    {;:db         (assoc db :messages/loading? true)
     :http-xhrio (wrap-get {:uri        "/api/konsent/message"
                            :on-success [:messages/set]
                            :on-failure [:common/set-error]})}))


(rf/reg-event-db
  :messages/set
  (fn [db [_ messages]]
    ;(println (str "event: set messages: " messages))
    (-> db
        (assoc :messages/loading? false
               :messages/list (reverse (:messages messages))))))


#_(rf/reg-sub
    :messages/loading?
    (fn [db _]
      (:messages/loading? db)))


(rf/reg-sub
  :messages/list
  (fn [db _]
    (:messages/list db)))



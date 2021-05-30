(ns decision-konsent.konsent-events
  (:require [decision-konsent.ajax :as ajax]
            [re-frame.core :as rf]))


(rf/reg-sub
  :konsent/list
  (fn [db _]
    (:konsent/list db)))


(rf/reg-sub
  :konsent/active
  (fn [db _]
    (:konsent/active db)))


(rf/reg-event-fx
  :konsent/load-list
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [fields (-> fields (assoc :owner (-> db :auth/user :email)))]
      (println "\n\nload list: " fields)
      {:http-xhrio (ajax/http-xhrio-get
                     {:uri        "/api/konsent/all-konsents-for-user"
                      :params     fields
                      :on-success [:konsent/handle-load-list]
                      :on-failure [:common/set-error-from-ajax]})})))


(rf/reg-event-db
  :konsent/handle-load-list
  (fn [db [_ data]]
    (println "\n\nget list: " data)
    (assoc db :konsent/list data)))


(rf/reg-event-fx
  :konsent/create
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [fields (-> fields (assoc :owner (-> db :auth/user :email))
                            (assoc :participants (decision-konsent.utils/emails-split (:participants fields))))]
      (println fields)
      {:http-xhrio (ajax/http-xhrio-post
                     {:uri        "/api/konsent/create-konsent"
                      :params     fields
                      :on-success [:konsent/activate]
                      :on-failure [:common/set-error-from-ajax]})
       :dispatch-n [[:konsent/load-list]]})))


(rf/reg-event-fx
   :konsent/activate
   (fn [{:keys [db] :as cofx} [_ data]]
     {:db (assoc db :konsent/active data)
      :dispatch [:common/navigate! :my-konsents nil nil]}))


(rf/reg-event-db
  :konsent/de-activate
  (fn [db [_ data]]
    (dissoc db :konsent/active)))
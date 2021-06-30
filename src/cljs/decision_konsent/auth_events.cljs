(ns decision-konsent.auth-events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [decision-konsent.ajax :refer [wrap-post wrap-get]]))



;;
;; ------------ subs -----------
;;

(rf/reg-sub
  :auth/user
  (fn [db _]
    (-> db :auth/user :email)))


(rf/reg-sub
  :auth/guest?
  (fn [db _]
    (and (-> db :auth/user :email)
         (= "guest" (-> db :auth/user :auth-type)))))

(rf/reg-sub
  :auth/register-worked
  (fn [db _]
    (-> db :auth/register-worked)))

;;
;; ---------- register -----------
;;

(rf/reg-event-fx
  :auth/start-register
  (fn [_world [_ fields]]
    ;(println "sending fields: " fields)
    {:http-xhrio (wrap-post {:uri        "/api/user/register"
                             :params     fields
                             :on-success [:auth/handle-register]
                             :on-failure [:common/set-error]})}))


(rf/reg-event-fx
  :auth/handle-register
  (fn [{:keys [db]} [_ {:keys [email]}]]
    ;(println "receiving identity: " identity)
    {:db       (-> db (assoc :auth/register-worked email))
     :dispatch [:common/navigate! :login nil nil]}))


;;
;; ---------- login -----------
;;


(rf/reg-event-fx
  :auth/start-login
  (fn [_world [_ fields]]
    ;(println "sending fields: " fields)
    {:http-xhrio (wrap-post {:uri        "/api/user/login"
                             :params     fields
                             :on-success [:auth/handle-login]
                             :on-failure [:common/set-error]})}))


(rf/reg-event-fx
  :auth/handle-login
  (fn [{:keys [db]} [_ {:keys [identity]}]]
    (println "\n:auth/handle-login " identity)
    {:db         (assoc db :auth/user identity)
     :dispatch-n [[:konsent/load-list]
                  [:common/navigate! :my-konsents nil nil]]}))


;;
;; ---------- logout -----------
;;

(rf/reg-event-fx
  :auth/start-logout
  (fn [_world [_ fields]]
    ;(println "sending logout: " fields)
    {:http-xhrio (wrap-post {:uri        "/api/user/logout"
                             :params     fields
                             :on-success [:auth/handle-logout]
                             :on-failure [:common/set-error]})}))

(rf/reg-event-fx
  :auth/handle-logout
  (fn [{:keys [db]} _]
    {:db       (-> db (dissoc :auth/user) (dissoc :konsent/active))
     :dispatch [:common/navigate! :login nil nil]}))


;;
;; ---------- session -----------
;;


(rf/reg-event-fx
  :session/load
  (fn [{:keys [db]} _]
    ;(println "reloading session - at least try...")
    {;:db         (assoc db :session/loading? true)
     :http-xhrio (wrap-get {:uri        "/api/user/session"
                            :on-success [:session/set]
                            :on-failure [:common/set-error]})}))


(rf/reg-event-fx
  :session/set
  (fn [{:keys [db]} [_ data]]
    (println "\n:session/set " data)
    (let [identity (-> data :session :identity)
          fx       (if identity
                     {:dispatch [:konsent/load-list]}
                     {})]
      (into fx {:db (assoc db :auth/user identity)}))))

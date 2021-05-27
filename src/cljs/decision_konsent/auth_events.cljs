(ns decision-konsent.auth-events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]))


(rf/reg-event-db
  :auth/handle-login
  (fn [db [_ {:keys [identity]}]]
    ;(println "receiving identity: " identity)
    (rf/dispatch [:common/navigate! :my-konsents nil nil])
    (assoc db :auth/user identity)))
; TODO
; switch to my-konsents


(rf/reg-event-db
  :auth/handle-register
  (fn [db [_ {:keys [identity]}]]
    ;(println "receiving identity: " identity)
    (assoc db :auth/register-worked "worked")))
; TODO
; switch to my-konsents

(rf/reg-event-db
  :auth/handle-login-error
  (fn [db [_ data]]
    ;(println "receiving error: " data)
    (assoc db :common/error (conj (db :common/error) (-> data :response :message)))))

(rf/reg-event-db
  :auth/handle-logout
  (fn [db _]
    (dissoc db :auth/user)))


(rf/reg-sub
  :auth/user
  (fn [db _]
    (-> db :auth/user :email)))

(rf/reg-sub
  :auth/register-worked
  (fn [db _]
    (-> db :auth/register-worked)))

(rf/reg-event-fx
  :auth/start-login
  (fn [_world [_ fields]]
    ;(println "sending fields: " fields)
    {:http-xhrio {:method          :post
                  :uri             "/api/user/login"
                  :params          fields
                  :timeout         5000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:auth/handle-login]
                  :on-failure      [:auth/handle-login-error]}}))

(rf/reg-event-fx
  :auth/start-register
  (fn [_world [_ fields]]
    ;(println "sending fields: " fields)
    {:http-xhrio {:method          :post
                  :uri             "/api/user/register"
                  :params          fields
                  :timeout         5000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:auth/handle-register]
                  :on-failure      [:auth/handle-login-error]}}))

(rf/reg-event-fx
  :auth/start-logout
  (fn [_world [_ fields]]
    ;(println "sending logout: " fields)
    {:http-xhrio {:method          :post
                  :uri             "/api/user/logout"
                  :params          fields
                  :timeout         5000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:auth/handle-logout]
                  :on-failure      [:auth/handle-login-error]}}))


(rf/reg-event-fx
  :session/load
  (fn [{:keys [db]} _]
    ;(println "reloading session - at least try...")
    {:db         (assoc db :session/loading? true)
     :http-xhrio {:method          :get
                  :uri             "/api/user/session"
                  :timeout         5000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:session/set]
                  :on-failure [:auth/handle-login-error]}}))

(rf/reg-event-db
  :session/set
  (fn [db [_ data]]
    ;(println  "\n\ngot session info: " data)
    (assoc db :auth/user (-> data :session :identity))))
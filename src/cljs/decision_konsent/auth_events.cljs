(ns decision-konsent.auth-events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]))



;;
;; ------------ subs -----------
;;

(rf/reg-sub
  :auth/user
  (fn [db _]
    (-> db :auth/user :email)))


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
    {:http-xhrio {:method          :post ; TODO: switch to http-xhrio-post
                  :uri             "/api/user/register"
                  :params          fields
                  :timeout         5000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:auth/handle-register]
                  :on-failure      [:auth/handle-login-error]}})) ; TODO: switch to :common/set-error-from-ajax


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
    {:http-xhrio {:method          :post ; TODO: switch to http-xhrio-post
                  :uri             "/api/user/login"
                  :params          fields
                  :timeout         5000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:auth/handle-login]
                  :on-failure      [:auth/handle-login-error]}})) ; TODO: switch to :common/set-error-from-ajax


(rf/reg-event-fx
  :auth/handle-login
  (fn [{:keys [db]} [_ {:keys [identity]}]]
    ;(println "receiving identity: " identity)
    {:db       (assoc db :auth/user identity)
     :dispatch [:common/navigate! :my-konsents nil nil]}))


(rf/reg-event-db
  :auth/handle-login-error ; TODO: switch to :common/set-error-from-ajax
  (fn [db [_ data]]
    ;(println "receiving error: " data)
    (assoc db :common/error (conj (db :common/error) (-> data :response :message)))))

;;
;; ---------- logout -----------
;;

(rf/reg-event-fx
  :auth/start-logout ; TODO: switch to http-xhrio-post
  (fn [_world [_ fields]]
    ;(println "sending logout: " fields)
    {:http-xhrio {:method          :post
                  :uri             "/api/user/logout"
                  :params          fields
                  :timeout         5000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:auth/handle-logout]
                  :on-failure      [:auth/handle-login-error]}})) ; TODO: switch to :common/set-error-from-ajax


(rf/reg-event-fx
  :auth/handle-logout
  (fn [{:keys [db]} _]
    {:db       (dissoc db :auth/user)
     :dispatch [:common/navigate! :login nil nil]}))

;;
;; ---------- session -----------
;;

(rf/reg-event-fx
  :session/load
  (fn [{:keys [db]} _]
    ;(println "reloading session - at least try...")
    {:db         (assoc db :session/loading? true)
     :http-xhrio {:method          :get ; TODO: switch to http-xhrio-get
                  :uri             "/api/user/session"
                  :timeout         5000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:session/set]
                  :on-failure      [:auth/handle-login-error]}})) ; TODO: ??? switch to :common/set-error-from-ajax


(rf/reg-event-db
  :session/set
  (fn [db [_ data]]
    ;(println  "\n\ngot session info: " data)
    (assoc db :auth/user (-> data :session :identity))))
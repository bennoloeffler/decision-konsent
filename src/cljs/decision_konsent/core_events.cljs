(ns decision-konsent.core-events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    ;[ajax.core :refer [GET POST]]
    [reitit.frontend.controllers :as rfc]
    [decision-konsent.client-time :as ct]))


;;
;; init
;;


(rf/reg-event-fx
  :initialize
  [(rf/inject-cofx :now)]
  (fn [cofx _]
    ;(println ":initialize event")
    {;:db         {:messages/loading? true :timestamp 0}     ; effects map
     :dispatch-n [[:timer (:now cofx)]
                  [:set-server-diff-time 0]
                  [:init-server-diff-time]
                  [:messages/load]
                  [:session/load]
                  [:gui-language :de]]}))


(rf/reg-cofx
  :now
  (fn [cofx _]
    (assoc cofx :now (ct/current-time))))


;;
;; ajax
;;


#_(rf/reg-fx ; TODO: switch to :common/set-error-from-ajax
    :ajax/get
    (fn [{:keys [url success-event error-event success-path]}]
      (println "start ajax/get...")
      (GET url
           (cond-> {:headers {"Accept" "application/transit+json"}}
                   success-event (assoc :handler
                                        #(rf/dispatch
                                           (conj success-event
                                                 (if success-path
                                                   (get-in % success-path)
                                                   %))))
                   error-event (assoc :error-handler
                                      #(rf/dispatch
                                         (conj error-event %)))))))



;;
;; navigate menu bar
;;

(rf/reg-event-db
  :common/navigate
  (fn [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :common/route new-match))))

(rf/reg-event-fx
    :common/navigate!
    (fn [_ [_ url-key params query]]
      {:common/navigate-fx! [url-key params query]}))

(rf/reg-fx
    :common/navigate-fx!
    (fn [[k & [params query]]]
      (rfe/push-state k params query)))

(rf/reg-sub
  :common/route
  (fn [db _]
    (-> db :common/route)))

(rf/reg-sub
  :common/page-id
  :<- [:common/route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :common/page
  :<- [:common/route]
  (fn [route _]
    (-> route :data :view)))

(rf/reg-sub
  :common/path-params
  :<- [:common/route]
  (fn [route _]
    (-> route :path-params)))

;;
;; error handling
;;

(rf/reg-event-db
  :common/clear-error
  (fn [db [_ _]]
    (dissoc db :common/error)))


(rf/reg-event-db
  :common/set-error
  (fn [db [_ data]]
    (println "receiving error from ajax: " data)
    ;(cljs.pprint/pprint data)
    (assoc db :common/error (conj (db :common/error)
                                  (or (-> data :response :message)
                                      (-> data :last-error not-empty)
                                      (str data))))))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))

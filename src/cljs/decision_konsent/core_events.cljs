(ns decision-konsent.core-events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    [ajax.core :refer [GET POST]]
    [reitit.frontend.controllers :as rfc]))



;;
;; init
;;

(rf/reg-event-fx
  :initialize
  (fn [_ _]
    ;(println ":initialize event")
    {:db       {:messages/loading? true} ; effects map
     :dispatch [:messages/load]}))

;;
;; ajax
;;


(rf/reg-fx
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

(rf/reg-fx
  :ajax/post
  (fn [{:keys [url success-event error-event success-path]}]
    (println "start ajax/post...   url: " url "    path: " success-path)
    (POST url
          (cond-> {:headers {"Accept" "application/transit+json"}}
                  success-event (assoc :handler
                                       #(do (rf/dispatch
                                              (conj success-event
                                                    (if success-path
                                                      (get-in % success-path)
                                                      %)))
                                            (println "got: " %)))
                  error-event (assoc :error-handler
                                     #(rf/dispatch
                                        (conj error-event %)))
                  true (assoc :params {:message "this is it..."})))))

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

#_(rf/reg-event-fx
    :common/navigate!
    (fn [_ [_ url-key params query]]
      {:common/navigate-fx! [url-key params query]}))

#_(rf/reg-fx
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

;;
;; error handling
;;

(rf/reg-event-db
  :common/clear-error
  (fn [db [_ _]]
    (dissoc db :common/error)))

(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error (conj (db :common/error) error))))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))
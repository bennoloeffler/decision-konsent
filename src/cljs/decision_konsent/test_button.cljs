(ns decision-konsent.test-button
  (:require [re-frame.core :as rf]
            [decision-konsent.ajax :as a]))

(defn test-button []
  [:a.button.is-warning.mr-1 {;:href     "#/not-doable"
                              :on-click #(rf/dispatch [:test-ajax-post {:some "thing"}])} ;unknown/event])}
   [:span.icon.is-large>i.fas.fa-1x.fa-flask]]) ;[:span "T"]])


(rf/reg-event-fx
  :test-ajax-get
  (fn [_world [_ fields]]
    (println "sending test-ajax-get: " fields)
    {:http-xhrio (a/wrap-get {:uri        "/api/konsent/test-ajax"
                              :params     fields
                              :on-success [:handle-ajax-success]
                              :on-failure [:common/set-error]})}))

(rf/reg-event-fx
  :test-ajax-post
  (fn [_world [_ fields]]
    (println "sending test-ajax-post: " fields)
    {:http-xhrio (a/wrap-post {:uri        "/api/konsent/test-ajax"
                               :params     fields
                               :on-success [:handle-ajax-success]
                               :on-failure [:common/set-error]})}))

(rf/reg-event-fx
  :handle-ajax-success
  (fn [_world [_ data]]
    (println "receiving test-ajax: " data)
    {}))
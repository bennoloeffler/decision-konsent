(ns decision-konsent.discussion
  (:require [reagent.ratom :as r]
            [ajax.core :refer [GET POST]]
            [re-frame.core :as rf]
            [decision-konsent.discussion-events]
            [decision-konsent.client-time :as ct]))



(def prevent-reload {:on-submit (fn [e] (do (.preventDefault e)
                                            (identity false)))})


(defn big-icon-button [tooltip-text icon-name]
  [:button.button.is-primary.is-outlined.mr-1 {:type "button" :data-tooltip tooltip-text :class "has-tooltip-arrow"}
   [:span.icon>i.fas.fa-1x {:class icon-name}]])


(defn konsent []
  [:span
   [big-icon-button "No concern.\nLet's do it." "fa-arrow-alt-circle-up"]
   [big-icon-button "Minor concern!\nHave to say them.\nBut then: Let's do it." "fa-arrow-alt-circle-right"]
   [big-icon-button "Major concern!\nThey need to be reflected.\nOnly if they are reflected,\nI could go with
     this suggestion." "fa-arrow-alt-circle-down"]
   [big-icon-button "VETO.\nI would like to take responsibility\nand make a next suggestion.\nYou should consider:
     This is really ultima ratio..." "fa-bolt"]
   [big-icon-button "Abstain from voting this time.\nI can live with whatever will be decided." "fa-comment-slash"]])


(defn small-icon-button
  ([icon-name] (small-icon-button nil icon-name))
  ([_ icon-name]                                            ; _ for :key
   [:button.button.is-rounded.is-light {:data-tooltip icon-name
                                        :class        "has-tooltip-arrow"
                                        :on-click     #(js/console.log icon-name)}
    [:span.icon.is-large>i.fas.fa-1x {:class icon-name}]]))


(defn small-icon-buttons [& icon-names]
  [:div (map (fn [icon-name] [small-icon-button {:key icon-name} (str icon-name)]) icon-names)])


(defn message-list [messages]
  (let [localtime @(rf/subscribe [:timestamp])
        server-diff @(rf/subscribe [:server-diff-time])]
    [:nav.panel
     [:p.panel-heading "feedbacks & comments"]
     (for [message @messages]
       ^{:key (:id message)}
       [:a.panel-block
        [:small.mr-3 (str "  ~" (ct/human-duration-sd (:timestamp message) localtime server-diff))]
        [:span.panel-icon
         [:i.fas.fa-book {:aria-hidden "true"}]]
        [:div.mr-3 (:message message)]])]))


(defn messages-form []
  ;(if @(rf/subscribe [:messages/loading?])
  ;[:form.box [:div "loading..."] [:button.button.is-loading]]
  [:div (message-list (rf/subscribe [:messages/list]))])




(defn discussion-form []
  (let [fields (r/atom {})]
    (fn []
      [:div
       [:form.box prevent-reload
        [:div.field.has-addons {:data-tooltip "leave a message, give feedback, suggest improvements..."}

         [:input.button.is-outlined.is-primary.mr-1
          {;:class  (if @(rf/subscribe [:messages/loading?])  "is-primary" "is-loading")
           :type     :button
           :value    "send"
           :on-click #(do (println "f: " @fields) (rf/dispatch [:message/save @fields]))}] ;#(send-message! fields)}]
         [:input.input
          {:type        :text
           :name        :message
           :on-change   #(swap! fields assoc :message (-> % .-target .-value))
           :on-key-down #(when (= (.-keyCode %) 13) (do (println "f: " @fields) (rf/dispatch [:message/save @fields])))
           :value       (:message @fields)}]]
        [messages-form]]])))



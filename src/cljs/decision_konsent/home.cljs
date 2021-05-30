(ns decision-konsent.home
  (:require [reagent.ratom :as r]
            [ajax.core :refer [GET POST]]
            [re-frame.core :as rf]
            [decision-konsent.home-events]
            [decision-konsent.client-time :as ct]
            [decision-konsent.utils :as utils]))


(defn big-icon-button [tooltip-text icon-name]
  [:button.button.is-primary.is-outlined.mr-1 {:type "button" :data-tooltip tooltip-text :class "has-tooltip-arrow"}
   [:span.icon>i.fas.fa-1x {:class icon-name}]])


(defn tutorial-text []
  [:div
   [:label.label "Mini-Tutorial: taking team decisions fast!"]
   [:div.mb-6 "Coming from a problem, first discuss possible options.
              Then, somebody suggests a specific solution, actions, etc. She or he is in charge to
              drive the decision.
              Now everybody can ask questions in order to understand the proposal.Don't start again discussing
              contradictions, your standpoint or feelings. As soon as everybody
              understand the details of the proposal, everybody gives hers or his agreement,
              concern or veto. If there are (only) agreements and minor concerns, the decision is taken.
              Perfect! If there are major concerns, they are spoken out and are worked into the proposal.
              In case of a veto, the person that issued the veto places the next proposal."
    [:strong " Please try the buttons below..."]]])


(defn tutorial-buttons []
  [:span
   [big-icon-button "No concern.\nLet's do it." "fa-arrow-alt-circle-up"]
   [big-icon-button "Minor concern!\nHave to say them.\nBut then: Let's do it." "fa-arrow-alt-circle-right"]
   [big-icon-button "Major concern!\nThey need to be reflected.\nOnly if they are reflected,\nI could go with
     this suggestion." "fa-arrow-alt-circle-down"]
   [big-icon-button "VETO.\nI would like to take responsibility\nand make a next suggestion.\nYou should consider:
     This is really ultima ratio..." "fa-bolt"]
   [big-icon-button "Abstain from voting this time.\nI can live with whatever will be decided." "fa-comment-slash"]])


(defn tutorial []
  [:div
   [tutorial-text]
   [tutorial-buttons]])


(defn small-icon-button
  ([icon-name] (small-icon-button nil icon-name))
  ([_ icon-name]                                            ; _ for :key
   [:button.button.is-rounded.is-light {:data-tooltip icon-name
                                        :class        "has-tooltip-arrow"
                                        :on-click     #(js/console.log icon-name)}
    [:span.icon.is-large>i.fas.fa-1x {:class icon-name}]]))


(defn small-icon-buttons [& icon-names]
  [:div (map (fn [icon-name] [small-icon-button {:key icon-name} (str icon-name)]) icon-names)])


(defn messages-form []
  (let [messages    @(rf/subscribe [:messages/list])
        localtime   @(rf/subscribe [:timestamp])
        server-diff @(rf/subscribe [:server-diff-time])]
    [:nav.panel
     [:p.panel-heading "feedbacks & comments"]
     (for [message messages]
       ^{:key (:id message)}
       [:a.panel-block
        [:span.panel-icon.ml-3
         [:i.fas.fa-book {:aria-hidden "true"}]]
        (:message message)
        [:span.panel-icon.ml-6
         [:i.fas.fa-clock {:aria-hidden "true"}]]
        [:small (str "  ~ " (ct/human-duration-sd (:timestamp message) localtime server-diff))]])]))



(defn discussion-form []
  (let [fields        (r/atom {})
        save-message! (fn []
                        (when (-> (:message @fields) count (> 0))
                          (rf/dispatch [:message/save @fields])
                          (reset! fields {})))]
    (fn []
      [:div
       [:form.box utils/prevent-reload
        [:div.field.has-addons {:data-tooltip "leave a message, give feedback, suggest improvements..."}

         [:input.button.is-outlined.is-primary.mr-1
          {;:class  (if @(rf/subscribe [:messages/loading?])  "is-primary" "is-loading")
           :type     :button
           :value    "send"
           :on-click #(save-message!)}]                             ;#(send-message! fields)}]
         [:input.input
          {:type        :text
           :name        :message
           :on-change   #(swap! fields assoc :message (-> % .-target .-value))
           :on-key-down #(when (= (.-keyCode %) 13) (save-message!))
           :value       (:message @fields)}]]
        [messages-form]]])))


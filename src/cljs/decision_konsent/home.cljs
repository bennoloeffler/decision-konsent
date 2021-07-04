(ns decision-konsent.home
  (:require [reagent.ratom :as r]
            [ajax.core :refer [GET POST]]
            [re-frame.core :as rf]
            [decision-konsent.home-events]
            [decision-konsent.client-time :as ct]
            [decision-konsent.utils :as utils]
            [decision-konsent.i18n :refer [tr]]))


(defn big-icon-button [tooltip-text icon-name]
  [:div.control>button.button.is-primary.is-outlined {:type "button" :data-tooltip tooltip-text :class "has-tooltip-arrow"}
   [:span.icon>i.fas.fa-1x {:class icon-name}]])


(defn tutorial-text []
  [:div
   [:label.label (tr [:h/lbl-tutorial] "Mini-Tutorial: taking team decisions fast!")]
   [:div.mb-6 (tr [:h/txt-tutorial] "")
    [:br]
    [:strong (tr [:h/txt-try-buttons-below] " Please try the buttons below...")]]])


(defn tutorial-buttons []
  [:div.field.has-addons
   [:div.control
    [:input.input {:type "text" :placeholder (tr [:h/inp-concerns-here] "your concerns here...")}]]
   [big-icon-button (tr [:h/too-no-concern] "No concern.\nLet's do it.") "fa-arrow-alt-circle-up"]
   [big-icon-button (tr [:h/too-minor] "Minor concern!\nHave to say it.\nBut then: Let's do it.") "fa-arrow-alt-circle-right"]
   [big-icon-button (tr [:h/too-major] "Major concern!\nThey need to be reflected.\nOnly if they are reflected,\nI could go with
     this suggestion.") "fa-arrow-alt-circle-down"]
   [big-icon-button (tr [:h/too-veto] "VETO.\nI would like to take responsibility\nand make a next suggestion.\nYou should consider:
     This is really ultima ratio...") "fa-bolt"]
   [big-icon-button (tr [:h/too-abstain] "Abstain from voting this time.\nI can live with whatever will be decided.") "fa-comment-slash"]])


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
     [:p.panel-heading (tr [:h/txt-feedback-comments] "feedbacks & comments")]
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
        [:div.field.has-addons {:data-tooltip (tr [:h/too-leave-a-comment] "leave a message, give feedback, suggest improvements...")}

         [:input.button.is-outlined.is-primary.mr-1
          {;:class  (if @(rf/subscribe [:messages/loading?])  "is-primary" "is-loading")
           :type     :button
           :value    (tr [:h/btn-send] "send")
           :on-click #(save-message!)}]                             ;#(send-message! fields)}]
         [:input.input
          {:type        :text
           :name        :message
           :on-change   #(swap! fields assoc :message (-> % .-target .-value))
           :on-key-down #(when (= (.-keyCode %) 13) (save-message!))
           :value       (:message @fields)}]]
        [messages-form]]])))


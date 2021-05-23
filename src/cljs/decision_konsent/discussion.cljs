(ns decision-konsent.discussion
  (:require [reagent.ratom :as r]
            [ajax.core :refer [GET POST]]
            [re-frame.core :as rf]
            [decision-konsent.discussion-events]))



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
   [big-icon-button "VETO.\nI would like to take responsibility\nand make a next suggestion.\nYou know:
     This is ultima ratio..." "fa-bolt"]])


(defn small-icon-button
  ([icon-name] (small-icon-button nil icon-name))
  ([_ icon-name] ; _ for :key
   [:button.button.is-rounded.is-light {:data-tooltip icon-name
                                        :class        "has-tooltip-arrow"
                                        :on-click     #(js/console.log icon-name)}
     [:span.icon.is-large>i.fas.fa-1x {:class icon-name}]]))


(defn small-icon-buttons [& icon-names]
  [:div (map (fn [icon-name] [small-icon-button {:key icon-name} (str icon-name)]) icon-names)])


(defn message-list [messages]
  [:nav.panel
    [:p.panel-heading "feedbacks & comments"]
    (for [message (map vector @messages (range))]
       ^{:key (str (first message) (last message))} ; TODO really an id...
       ;[:div.list-item>li message]
       [:a.panel-block
         [:span.panel-icon
          [:i.fas.fa-book {:aria-hidden "true"}]] (first message)])])


(defn messages-form []
  ;(if @(rf/subscribe [:messages/loading?])
    ;[:form.box [:div "loading..."] [:button.button.is-loading]]
    [:div (message-list (rf/subscribe [:messages/list]))])

(defn login []
  [:form.box prevent-reload
   [:div.field
    [:label.label "Email"]
    [:div.control
     [:input.input {:type "email" :placeholder "e.g. alex@example.com"}]]]
   [:div.field
    [:label.label "Password"]
    [:div.control
     [:input.input {:type "password" :placeholder "********"}]]]

   [:button.button.is-primary.mr-1.mt-3 "Sign in"]
   [:button.button.is-primary.is-outlined.mt-3 "No account? Create an account..."]])

(defn register []
  [:form.box prevent-reload
   [:div.field
    [:label.label "Email"]
    [:div.control
     [:input.input {:type "email" :placeholder "e.g. alex@example.com"}]]]
   [:div.field
    [:label.label "Password"]
    [:div.control
     [:input.input {:type "password" :placeholder "********"}]]]
   [:div.field
    [:label.label "Confirm Password"]
    [:div.control
     [:input.input {:type "Password" :placeholder "********"}]]]
   [:div
    [:button.button.is-primary.mr-1.mt-3 "Create your account"]
    [:button.button.is-primary.is-outlined.mt-3 "I have an account. Just login."]]])



(defn discussion-form []
 (let [fields (r/atom {})]
   (fn []
    [:div
     [:form.box prevent-reload
         [:div.field.has-addons {:data-tooltip "leave a message, give feedback, suggest improvements..."}

           [:input.button.is-outlined.is-primary.mr-1
             {;:class  (if @(rf/subscribe [:messages/loading?])  "is-primary" "is-loading")
              :type :button
              :value "send"
              :on-click #(do (println "f: " @fields) (rf/dispatch [:message/save @fields]))}] ;#(send-message! fields)}]
           [:input.input
            {:type        :text
             :name        :message
             :on-change   #(swap! fields assoc :message (-> % .-target .-value))
             :on-key-down #(when (= (.-keyCode %) 13)  (do (println "f: " @fields) (rf/dispatch [:message/save @fields])))
             :value       (:message @fields)}]]
         [messages-form]]])))



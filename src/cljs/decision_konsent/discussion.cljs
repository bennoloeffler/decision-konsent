(ns decision-konsent.discussion
  (:require [reagent.ratom :as r]
            [ajax.core :refer [GET POST]]
            [re-frame.core :as rf]))

(defn big-icon-button [tooltip-text icon-name]
  [:button.button.is-primary.is-outlined { :data-tooltip tooltip-text :class "has-tooltip-arrow"} [:span.icon>i.fas.fa-1x {:class icon-name}]])

(defn konsent []
  [:span
   [big-icon-button "No concern.\nLet's do it." "fa-arrow-alt-circle-up"]
   [big-icon-button "Minor concern!\nHave to say them.\nBut then: Let's do it." "fa-arrow-alt-circle-right"]
   [big-icon-button "Major concern!\nThey need to be reflected.\nOnly if they are reflected,\nI could go with this suggestion." "fa-arrow-alt-circle-down"]
   [big-icon-button "VETO.\nI would like to take responsibility\nand make a next suggestion.\nYou know: This is ultima ratio..." "fa-bolt"]])

(defn small-icon-button
  ([icon-name] (small-icon-button nil icon-name))
  ([_ icon-name]
   [:button.button.is-rounded.is-light {:data-tooltip icon-name
                                        :class        "has-tooltip-arrow"
                                        :on-click     #(js/console.log icon-name)}
     [:span.icon.is-large>i.fas.fa-1x {:class icon-name}]]))

(defn small-icon-buttons [& icon-names]
  [:div (map (fn [icon-name] [small-icon-button {:key icon-name} (str icon-name)]) icon-names)])



(defn send-message! [fields]
  (POST "/api/konsent/message"
        {:params @fields
         :handler #(do ( .log js/console (str %))
                       (rf/dispatch [:messages/load]))
         :error-handler #(.error js/console (str "error: " %))}))


#_(defn message-list [messages]
    [:div.container>div.list>ul
     (for [message @messages]
       ^{:key message}
       [:div.list-item>li message])])

(defn message-list [messages]
  [:nav.panel
    [:p.panel-heading "feedbacks & comments"]
    (for [message @messages]
       ^{:key message}
       ;[:div.list-item>li message]
       [:a.panel-block
         [:span.panel-icon
          [:i.fas.fa-book {:aria-hidden "true"}]] message])])


(defn messages-form []
  (if @(rf/subscribe [:messages/loading?])
    [:div "loading messages..."]
    [:div (message-list (rf/subscribe [:messages/list]))]))

(defn discussion-form []
 (let [fields (r/atom {})]
   (fn []
    [:div
      [:div.field.has-addons {:data-tooltip "leave a message, give feedback, suggest improvements..."}
        [:input.button.is-primary
          {:type :submit
           :value "send"

           :on-click #(send-message! fields)}]
        [:input.input
          {:type :text
           :name :message
           :on-change #(swap! fields assoc :message (-> % .-target .-value))
           :value (:message @fields)}]]
      [messages-form]])))



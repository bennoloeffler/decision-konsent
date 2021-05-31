(ns decision-konsent.konsent
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [decision-konsent.utils :as utils]
            [decision-konsent.konsent-events]))


(def login-to-start "first login.\nthen start.")
(def not-logged-in {:data-tooltip login-to-start
                    :class        [:has-tooltip-warning :has-tooltip-active :has-tooltip-arrow]
                    :disabled     true})


(defn new-konsent-page []
  (let [fields (r/atom {})]
    (fn []
      [:section.section>div.container>div.content
       [:div.field
        [:form.box utils/prevent-reload
         [:h1.title.is-4 "create new konsent"]
         [:div.field
          [:label.label "short name"]
          [:div.control
           [:input.input {:type        "text"
                          :placeholder "e.g. old coffee machine broke - new, which?"
                          :on-change   #(swap! fields assoc :short-name (-> % .-target .-value))}]]]
         [:div.field
          [:label.label "problem & background"]
          [:div.control
           [:textarea.textarea {:type        "text"
                                :placeholder "e.g. there are plenty of options and plenty of price options - and nobody really knows the read needs, habbis of the group. Even if there should be a common coffee machine."
                                :on-change   #(swap! fields assoc :problem-statement (-> % .-target .-value))}]]]
         [:div.field
          [:label.label "participants"]
          [:div.control
           [:textarea.textarea {:type        "text"
                                :placeholder "e.g. hugo.huelsensack@the-company.com\nbruno.banani@the-same-company.com\nmarc.more@the-company.com"
                                :rows        "10"
                                :on-change   #(swap! fields assoc :participants (-> % .-target .-value))}]]]

         [:div [:button.button.is-primary.mt-3.has-tooltip-right
                (if @(rf/subscribe [:auth/user])
                  {:on-click #(rf/dispatch [:konsent/create @fields])}
                  not-logged-in)
                "create the konsent"]]]]])))


(defn example []
  [:div
   [:a.panel-block
    [:span.panel-icon
     [:i.fas.fa-book {:aria-hidden "true"}]] "new bib - or not..."]
   [:a.panel-block
    [:span.panel-icon
     [:i.fas.fa-bomb {:aria-hidden "true"}]] "konsent about the new coffee machine"]
   [:a.panel-block
    [:span.panel-icon
     [:i.fas.fa-fighter-jet {:aria-hidden "true"}]] "decision regarding new traveling rules"]])


(defn konsents-of-user []
  "show all konsents for user based on data [{:id 12 :short-name 'abc' :timestamp :changes} ...]"
  (let [konsents @(rf/subscribe [:konsent/list])]
    [:div
     (for [konsent-info konsents]
       [:a.panel-block
        {:key (:id konsent-info)}
        [:span.panel-icon
         {:on-click #(rf/dispatch [:konsent/delete konsent-info])
          :data-tooltip "delete?"
          :class [:has-tooltip-warning :has-tooltip-arrow]}
         [:i.fas.fa-archive]]
        [:span {:on-click #(rf/dispatch [:konsent/activate konsent-info])}
         (get-in konsent-info [:konsent :short-name])]])]))


(defn konsent-list []
  [:div
   [:p.panel-heading
    [:div.columns
     [:div.column.is-3
      [:button.button.is-primary
       (if @(rf/subscribe [:auth/user])
         {:on-click #(rf/dispatch [:common/navigate! :new-konsent nil nil])}
         not-logged-in)
       "create new"]]
     [:div.column.is-9 "all my-konsents"]]]
   (if @(rf/subscribe [:auth/user])
     [konsents-of-user]
     [example])])


(defn konsent-active []
  #_[:a.panel-block (str @(rf/subscribe [:konsent/active]))]
 (let [konsent-info @(rf/subscribe [:konsent/active])]
  [:div
   [:p.panel-heading
    [:div.columns
     [:div.column.is-3
      [:button.button.is-primary
       (if @(rf/subscribe [:auth/user])
         {:on-click #(rf/dispatch [:konsent/de-activate])}
         not-logged-in)
       "back to all konsents"]]
     [:div.column.is-9 ""]]]
   (if @(rf/subscribe [:auth/user])
     ^{:key (:id konsent-info)}
     [:a.panel-block
      (str konsent-info)]
     [example])]))


(defn my-konsents-page []
  [:section.section>div.container>div.content
   [:nav.panel
    (if @(rf/subscribe [:konsent/active])
      [konsent-active]
      [konsent-list])]])


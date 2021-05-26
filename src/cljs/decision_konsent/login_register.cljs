(ns decision-konsent.login-register
  (:require [decision-konsent.client-time :as ct]))


(def prevent-reload {:on-submit (fn [e] (do (.preventDefault e)
                                            (identity false)))})

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

   [:button.button.is-primary.mr-1.mt-3
    {;:class  (if @(rf/subscribe [:messages/loading?])  "is-primary" "is-loading")
     :type     :button
     ;:value    "Sign in"
     :on-click #(ct/get-server-time!)} "Sign in"]
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

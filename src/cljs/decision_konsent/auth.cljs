(ns decision-konsent.auth
  (:require [decision-konsent.client-time :as ct]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [decision-konsent.auth-events]))


(def prevent-reload {:on-submit (fn [e] (do (.preventDefault e)
                                            (identity false)))})

(defn login []
  (let [fields (r/atom {:email "nothing typed" :password "nothing too"})]
    (fn []
      [:section.section>div.container>div.content
       [:form.box prevent-reload
        [:h1.title.is-4 "login"]
        [:h2.subtitle.is-6.mb-6 "login to your account with your email and password"]
        [:div.field.
         [:label.label "Email"]
         [:div.control
          [:input.input {:type        "email"
                         :placeholder "e.g. alex@example.com"
                         :on-change   #(swap! fields assoc :email (-> % .-target .-value))}]]]
        [:div.field
         [:label.label "Password"]
         [:div.control
          [:input.input {:type        "password"
                         :placeholder "********"
                         :on-change   #(swap! fields assoc :password (-> % .-target .-value))}]]]

        [:button.button.is-primary.mr-1.mt-3
         {:on-click #(rf/dispatch [:auth/start-login @fields])}
         (if (not @(rf/subscribe [:auth/user]))
           "login"
           "you are logged in :-)")]
        #_[:button.button.is-primary.is-outlined.mt-3 "No account? Create an account..."]]])))

(defn register []
  (let [fields (r/atom {})]
    (fn []
      [:section.section>div.container>div.content
       [:form.box prevent-reload
        [:h1.title.is-4 "register"]
        [:h2.subtitle.is-6.mb-6 "register with a valid email and choose a password"]
        [:div.field
         [:label.label "Email"]
         [:div.control
          [:input.input {:type        "email"
                         :placeholder "e.g. alex@example.com"
                         :on-change   #(swap! fields assoc :email (-> % .-target .-value))}]]]
        [:div.field
         [:label.label "Password"]
         [:div.control
          [:input.input {:type        "password"
                         :placeholder "********"
                         :on-change   #(swap! fields assoc :password (-> % .-target .-value))}]]]
        [:div.field
         [:label.label "Confirm Password"]
         [:div.control
          [:input.input {:type        "Password"
                         :placeholder "********"
                         :on-change   #(swap! fields assoc :confirm (-> % .-target .-value))}]]]
        [:div
         [:button.button.is-primary.mr-1.mt-3
          {:on-click #(rf/dispatch [:auth/start-register @fields])}
          "register your account"]
         #_[:button.button.is-primary.is-outlined.mt-3 "I have an account. Just login."]]]])))

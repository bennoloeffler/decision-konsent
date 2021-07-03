(ns decision-konsent.auth
  (:require [decision-konsent.client-time :as ct]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [decision-konsent.auth-events]
            [decision-konsent.utils :as utils]
            [decision-konsent.i18n :as i18n :refer [tr]]))


(defn register []
  (let [fields (r/atom {})]
    (fn []
      [:section.section>div.container>div.content
       [:form.box utils/prevent-reload
        [:h1.title.is-4 (tr [:a/txt-register] "register")]
        [:h2.subtitle.is-6.mb-6 (tr [:a/txt-register-email-password] "register with a valid email and choose a password")]
        [:div.field
         [:label.label (tr [:a/lbl-login-email] "email")]
         [:div.control
          [:input.input {:type        "email"
                         :placeholder "e.g. alex@example.com"
                         :on-change   #(swap! fields assoc :email (-> % .-target .-value))}]]]
        [:div.field
         [:label.label (tr [:a/lbl-login-password] "password")]
         [:div.control
          [:input.input {:type        "password"
                         :placeholder "********"
                         :on-change   #(swap! fields assoc :password (-> % .-target .-value))}]]]
        [:div.field
         [:label.label (tr [:a/lbl-register-password] "confirm password")]
         [:div.control
          [:input.input {:type        "Password"
                         :placeholder "********"
                         :on-change   #(swap! fields assoc :confirm (-> % .-target .-value))}]]]
        [:div
         [:button.button.is-primary.mr-1.mt-3
          {:on-click #(rf/dispatch [:auth/start-register @fields])}
          [:span.icon.is-large>i.fas.fa-1x.fa-pen-nib]
          [:div (tr [:a/btn-register-now] "register your account now")]]
         (when-let [registered @(rf/subscribe [:auth/register-worked])] [:h2.subtitle.is-6 (str "you sucessfully registered: " registered)])]]])))


(defn login []
  (let [fields (r/atom {:email (or @(rf/subscribe [:auth/register-worked]) "") :password "nothing too"})]
    (fn []
      [:section.section>div.container>div.content
       [:form.box utils/prevent-reload
        [:h1.title.is-4 (tr [:a/txt-login])]
        [:h2.subtitle.is-6.mb-6 (tr [:a/txt-email-password])]
        [:div.field
         [:label.label (tr [:a/lbl-login-email] "email")]
         [:div.control
          [:input.input {:type        "email"
                         :value       (:email @fields)
                         :placeholder "e.g. alex@example.com"
                         :on-change   #(swap! fields assoc :email (-> % .-target .-value))}]]]
        [:div.field
         [:label.label (tr [:a/lbl-login-password] "password")]
         [:div.control
          [:input.input {:type        "password"
                         :placeholder "********"
                         :on-change   #(swap! fields assoc :password (-> % .-target .-value))}]]]


        (if (not @(rf/subscribe [:auth/user]))
          [:button.button.is-primary.mr-1.mt-3
           {:on-click #(rf/dispatch [:auth/start-login @fields])}
           [:span.icon.is-large>i.fas.fa-1x.fa-sign-in-alt]
           [:div (tr [:a/btn-login-now])]]
          [:h2.subtitle.is-6 (tr [:a/txt-you-are-logged-in] "you are logged in :-)")])]])))


(ns decision-konsent.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [decision-konsent.ajax :as ajax]
    [decision-konsent.core-events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string]
    [decision-konsent.home :as h]
    [ajax.core :refer [GET POST]]
    [decision-konsent.client-time :as ct]
    [decision-konsent.auth :as l])
  (:import goog.History))

(def prevent-reload {:on-submit (fn [e] (do (.preventDefault e)
                                            (identity false)))})

(def logged-in (r/atom false))

(defn login-button []
  [:a.button.is-primary {:href "#/login"} #_{:on-click #(reset! logged-in true)} [:span.icon.is-large>i.fas.fa-1x.fa-sign-in-alt] [:span "login"]])

(defn register-button []
  [:a.button.is-warning.mr-1 {:href "#/register"} #_{:on-click #(rf/dispatch [:common/set-error "registration is not yet available - sorry"])} [:span.icon.is-large>i.fas.fa-1x.fa-pen-nib] [:span "register an account and then..."]])

(defn logout-button []
  [:button.button.is-primary.is-outlined {:on-click #(rf/dispatch [:auth/start-logout])} [:span.icon.is-large>i.fas.fa-1x.fa-sign-out-alt] [:span "logout"]])

(defn user-indicator []
  [:button.button.is-primary.is-outlined.mr-1 {:on-click #(rf/dispatch [:common/set-error "profile editing not yet available - sorry"])} [:span.icon.is-large>i.fas.fa-1x.fa-address-card] [:span @(rf/subscribe [:auth/user])]])


(defn nav-link [uri title page]
  [:a.navbar-item
   {:href  uri
    :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
   title])


(defn navbar []
  (r/with-let [expanded? (r/atom false)]
              [:nav.navbar.is-light>div.container
               [:div.navbar-brand.
                ;[:a.navbar-item [:img {:src "img/logo-100x100-gelb-trans.png" :alt "konsent"}]]
                [:a.navbar-item {:href "/" :style {:font-weight :bold}} [:img {:src "img/logo-100x100-gelb-trans.png" :alt "konsent"}]]
                [:span.navbar-burger.burger
                 {:data-target :nav-menu
                  :on-click    #(swap! expanded? not)
                  :class       (when @expanded? :is-active)}
                 [:span] [:span] [:span]]]
               [:div#nav-menu.navbar-menu
                {:class (when @expanded? :is-active)}
                [:div.navbar-start
                 [nav-link "#/" "home" :home]
                 [nav-link "#/my-konsents" "my-konsents" :my-konsents]
                 [nav-link "#/about" "about" :about]]
                [:div.navbar-end
                 [:div.navbar-item
                  (if (not @(rf/subscribe [:auth/user]))
                    [:div.buttons
                     [register-button]
                     [login-button]]
                    [:div.buttons
                     [user-indicator]
                     [logout-button]])]]]]))


(defn about-page []
  [:section.section>div.container.is-vertical.is-flexible
   [:div.column.is-one-third
    [:article.message.is-primary
     [:div.message-header
      [:p "about..."] [:div.card
                       [:div.card-image
                        [:figure.image.is-3by3
                         [:img {:src "img/logo-100x100-gelb-trans.png" :alt "Placeholder image"}]]]]
      #_[:button.delete {:aria-label "delete"}]]
     [:div.message-body "konsent with a K -
     neither 'to consent' nor consensus.
     Basically, it's decision making based on the absence of objections or major concerns. "
      [:a {:href "https://thedecider.app/consent-decision-making"} "See here for more info."]
      " Made be Armin and Benno."]]]])


(defn my-konsents-page []
  [:section.section>div.container>div.content
   [:nav.panel
    [:p.panel-heading "my-konsents"]
    [:a.panel-block
     [:span.panel-icon
      [:i.fas.fa-book {:aria-hidden "true"}]] "new bib - or not..."]
    [:a.panel-block
     [:span.panel-icon
      [:i.fas.fa-bomb {:aria-hidden "true"}]] "konsent about the new coffee machine"]
    [:a.panel-block
     [:span.panel-icon
      [:i.fas.fa-fighter-jet {:aria-hidden "true"}]] "decision regarding new traveling rules"]]])


(defn home-page []
  [:section.section>div.container>div.content
   [:div.field.mb-6
    [:form.box
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
      [:strong " Please try the buttons below..."]]

     [h/tutorial]]]
   ;[d/login]
   ;[d/register]
   [h/discussion-form]])



(defn errors-section []
  (if-let [errs @(rf/subscribe [:common/error])]
    [:section.section>div.container>div.content
     [:article.message.is-warning
      [:div.message-header
       [:p "a charming warning from the machine..."]
       [:button.delete {:aria-label "delete" :on-click #(rf/dispatch [:common/clear-error])}]]
      (for [err (map vector errs (range))]
        ^{:key (str (first err) (last err))}
        [:div.message-body (first err)])]]))


(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div.column.is-two-thirds
     [navbar]
     [:div.columns.is-centered>div.column
      [errors-section]
      [page]]]))


(defn navigate! [match _]
  ;(println (str "navigate! match = " match _))
  (rf/dispatch [:common/navigate match]))


(def router
  (reitit/router
    [["/" {:name :home
           :view #'home-page
           #_[:controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]]}]

     ["/about" {:name :about
                :view #'about-page}]

     ["/login" {:name :login
                :view #'l/login}]

     ["/register" {:name :register
                   :view #'l/register}]

     ["/my-konsents" {:name :my-konsents
                      :view #'my-konsents-page}]]))


(defn start-router! []
  (println "starting up ROUTER")
  (rfe/start!
    router
    navigate!
    {}))


;;
;; Initialize app
;;
(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))


(defn init! []
  (println "starting up konsent CLIENT SPA")
  (start-router!)
  (ajax/load-interceptors!)
  (rf/dispatch-sync [:initialize])
  (mount-components))

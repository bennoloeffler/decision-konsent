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
    [decision-konsent.konsent :as k]
    [ajax.core :refer [GET POST]]
    [decision-konsent.client-time :as ct]
    [decision-konsent.auth :as l]
    [decision-konsent.websocket :as ws]
    [decision-konsent.test-button :as tb]
    [decision-konsent.i18n :as i18n :refer [tr]]
    [reagent.core :as reagent]
    [spec-tools.data-spec :as ds])
  ;["@creativebulma/bulma-tagsinput/dist/js/bulma-tagsinput.min.js" :as tags])

  (:import goog.History))



#_(def logged-in (r/atom false))

(defn login-button []
  [:a.button.is-primary {:href "#/login"} #_{:on-click #(reset! logged-in true)} [:span.icon.is-large>i.fas.fa-1x.fa-sign-in-alt] [:span (tr [:a/btn-login] "login")]])

(defn register-button []
  [:a.button.is-warning.mr-1
   {:href "#/register" :on-click #(rf/dispatch [:messages/load])}
   [:span.icon.is-large>i.fas.fa-1x.fa-pen-nib]
   [:span (tr [:n/btn-register])]])

(defn logout-button []
  [:button.button.is-primary.is-outlined {:on-click #(rf/dispatch [:auth/start-logout])} [:span.icon.is-large>i.fas.fa-1x.fa-sign-out-alt] [:span "logout"]])

(defn user-indicator []
  (let [guest? @(rf/subscribe [:auth/guest?])]
    [:button.button.is-primary.is-outlined
     {:on-click #(rf/dispatch [:common/set-error "profile editing not yet available - sorry"])
      :class    (when guest? :mr-5)}
     [:span.icon.is-large>i.fas.fa-1x.fa-address-card]
     [:span @(rf/subscribe [:auth/user])]
     (when guest? [:span.badge.is-info (tr [:k/guest])])]))


(defn nav-link [uri title page]
  [:a.navbar-item
   {:href  uri
    :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
   title])


(defn navbar []
  (let [guest? @(rf/subscribe [:auth/guest?])]
    (r/with-let [expanded? (r/atom false)]
                [:nav.navbar.is-light>div.container
                 [:div.navbar-brand.
                  ;[:a.navbar-item [:img {:src "img/logo-100x100-gelb-trans.png" :alt "konsent"}]]
                  [:a.navbar-item.ml-3 {:href "/" :style {:font-weight :bold}} [:img {:src "img/logo-100x100-gelb-trans.png" :alt "konsent"}]]
                  [:span.navbar-burger.burger
                   {:data-target :nav-menu
                    :on-click    #(swap! expanded? not)
                    :class       (when @expanded? :is-active)}
                   [:span] [:span] [:span]]]
                 [:div#nav-menu.navbar-menu
                  {:class (when @expanded? :is-active)}
                  [:div.navbar-start
                   [nav-link "#/" (tr [:n/home]) :home]
                   [nav-link "#/my-konsents" (tr [:n/my-konsent]) :my-konsents]
                   (when-not guest? [nav-link "#/new-konsent" (tr [:n/new-konsent]) :new-konsent])
                   [nav-link "#/about" (tr [:n/about]) :about]]
                  [:div.navbar-end
                   [:div.navbar-item.mr-3

                    ;[tb/test-button]
                    (if (not @(rf/subscribe [:auth/user]))
                      [:div.buttons
                       [register-button]
                       [login-button]]
                      [:div.buttons
                       [user-indicator]
                       [logout-button]])]
                   [:div.navbar-item.has-dropdown.is-hoverable
                    [:a.navbar-link @(rf/subscribe [:gui-language-show])]
                    [:div.navbar-dropdown
                     [:a.navbar-item {:on-click #(rf/dispatch [:gui-language :en])} (i18n/language-name :en)]
                     [:a.navbar-item {:on-click #(rf/dispatch [:gui-language :de])} (i18n/language-name :de)]]]]]])))







(defn about-page []
  [:section.section>div.container.is-vertical.is-flexible
   [:div.column.is-two-thirds
    [:article.message.is-primary
     [:div.message-header
      [:div (tr [:about/header] "about...")] [:div.card]
      #_[:div.card-image
         [:figure.image.is-3by3
          [:img {:src "img/logo-100x100-gelb-trans.png" :alt "Placeholder image"}]]]
      #_[:button.delete {:aria-label "delete"}]]
     [:div.message-body (tr [:about/message] "konsent with a K...")
      [:div [:a {:href "https://thedecider.app/consent-decision-making"} (tr [:about/see-more] "See here for more info.")]]
      [:br]
      [:div (tr [:about/who] "Made be Armin and Benno.")]
      [:br]
      [:div (tr [:about/thanks] "Thanks to the people involved in those projects, we build upon:")]
      [:br]
      [:div "clojure"]
      [:div "shadow-cljs"]
      [:div "http-kit"]
      [:div "ring"]
      [:div "luminus"]
      [:div "re-frame"]
      [:div "postgres"]
      [:div "heroku"]
      [:div "bulma"]
      [:div "buddy"]
      [:div "tempura"]
      [:div "java"]]]]])


(defn home-page []
  [:section.section>div.container>div.content
   [:div.field.mb-6
    [:form.box
     [h/tutorial]]]
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



(defn tag-input-data []
  (let [inputTags (.getElementById js/document "tags-input-id")
        ;_         (js/BulmaTagsInput. inputTags)
        _         (println "inputTags: " inputTags)
        bti       (when inputTags (. inputTags BulmaTagsInput))
        _         (println "bti: " bti)
        bti-val   (when bti (.-value bti))]
    bti-val))

; (. js/BulmaTagsInput attach)
(defn attach []
  (let [TAGS (js/BEL)
        _    (.attachtags TAGS)]))

(defn value [element]
  (-> element .-target .-value))

(defn mail-model []
  [:<>
   [:option {:value "benno.loeffler@gmx.net" :selected true} "benno.loeffler@gmx.net"]
   [:option {:value "Ralf.Kapp@arcor.de" :selected true} "Ralf.Kapp@arcor.de"]
   [:option {:value "sabine.kiefer@gmx.de"} "sabine.kiefer@gmx.de"]])

(defn tags-input-2 []
  (let [fields (reagent/atom {})]                           ;; you can include state
    (reagent/create-class
      {:component-did-mount
                     (fn [] (println "I mounted + attached") (attach))

       ;; ... other methods go here
       ;; see https://facebook.github.io/react/docs/react-component.html#the-component-lifecycle
       ;; for a complete list

       ;; name your component for inclusion in error messages
       :display-name "complex-component"

       ;; note the keyword for this method
       :reagent-render
                     (fn []
                       [:div.field
                        [:label.label "tags"]
                        [:div.control
                         [:button.button {:on-click #(println "click:" (tag-input-data))} "save"]
                         [:div.field
                          [:div.control
                           ;type="text" data-item-text="name" data-item-value="numericCode" data-case-sensitive="false" placeholder="Try finding a country name" value=""
                           #_[:input#tags-input-id.input {:type                "tags"
                                                          :data-item-text=     "text"
                                                          :data-item-value=    "value"
                                                          :data-case-sensitive false
                                                          ;:multiple true
                                                          :value               ""
                                                          :data-type           "tags"
                                                          :data-placeholder    "Choose Email - start typing..."
                                                          :source              [{"value" 1 "text" "One"},
                                                                                {"value" 2 " text" "Two"}]}]


                           [:select#tags-input-id {
                                                   :type             "tags"
                                                   :multiple         true
                                                   ;:free-input       true
                                                   :data-type        "tags"
                                                   :data-placeholder "Choose Tags"
                                                   ;:data-item-text   "text" :data-item-value "value"
                                                   :source           ["Saab", "Volvo", "BMW"]}

                            [mail-model]]]]]])})))


;(swap! fields assoc :tags (value %)))}]]])})))


(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div.column                                            ;.is-two-thirds
     [navbar]
     [:div.columns.is-centered>div.column
      ;[tags-input-2]
      [errors-section]
      [page @(rf/subscribe [:common/route])]]]))



(defn navigate! [match history]
  ;(println "navigate! match = " match "\nhistory = " history)
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
                      :view #'k/my-konsents-page}]

     ["/my-konsent/:id" {:name :my-konsent
                         :view #'k/my-konsent-page
                         :parameters {:path {:id int?}}
                         :controllers
                         [{:parameters {:path [:id]}
                           :start (fn [params] (js/console.log "Entering my-konsent: " params)
                                               (rf/dispatch [:konsent/activate  (-> params :path)]))
                           :stop  (fn [params] (js/console.log "Leaving my-konsent: " params))}]}]


     ["/new-konsent" {:name :new-konsent
                      :view #'k/new-konsent-page}]]))


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
;(. js/BulmaTagsInput attach))
;(attach))
; https://github.com/helins/timer.cljs




(defn init! []
  (println "starting up konsent CLIENT SPA")
  ;(ws/init!)
  (start-router!)
  (ajax/load-interceptors!)

  (rf/dispatch-sync [:initialize])
  (mount-components))



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
    [decision-konsent.events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string]
    [decision-konsent.discussion :as d]
    [ajax.core :refer [GET POST]])

  (:import goog.History))



;; register event-handler (rf/reg-event-db)
(rf/reg-event-fx
  :initialize
  (fn [_ _]
    (println ":initialize event")
    {:db {:messages/loading? true}
     :dispatch [:messages/load]}))

(rf/reg-fx
  :ajax/get
  (fn [{:keys [url success-event error-event success-path]}]
    (println "start ajax...")
    (GET url
      (cond-> {:headers {"Accept" "application/transit+json"}}
        success-event (assoc :handler
                             #(rf/dispatch
                                (conj success-event
                                      (if success-path
                                        (get-in % success-path)
                                        %))))
        error-event (assoc :error-handler
                           #(rf/dispatch
                              (conj error-event %)))))))

(rf/reg-event-fx
  :messages/load
  (fn [{:keys [db]} _]
    (println "start loading...")
    {:db       (assoc db :messages/loading? true)
     :ajax/get {:url           "/api/konsent/message"
                :success-path  [:messages]
                :success-event [:messages/set]}}))

(rf/reg-event-db
  :messages/set
  (fn [db [_ messages]]
    (println ( str "set messages: " messages))
    (-> db
        (assoc :messages/loading? false
               :messages/list messages))))

;; register subscription
(rf/reg-sub
  :messages/loading?
  (fn [db _]
    (:messages/loading? db)))

(rf/reg-sub
  :messages/list
  (fn [db _]
    (reverse (:messages/list db))))

(comment

  ;; use subscription in hiccup ???
  (if @(rf/subscribe [:message/loading?]))

  ; fire event
  (rf/dispatch [:initialize]))



(defn nav-link [uri title page]
  [:a.navbar-item
   {:href  uri
    :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
              [:nav.navbar.is-info>div.container
               [:div.navbar-brand
                [:a.navbar-item {:href "/" :style {:font-weight :bold}} "decision-konsent"]
                [:span.navbar-burger.burger
                 {:data-target :nav-menu
                  :on-click    #(swap! expanded? not)
                  :class       (when @expanded? :is-active)}
                 [:span] [:span] [:span]]]
               [:div#nav-menu.navbar-menu
                {:class (when @expanded? :is-active)}
                [:div.navbar-start
                 [nav-link "#/" "home" :home]
                 [nav-link "#/my-konsents" "myKonsents" :my-konsents]
                 [nav-link "#/about" "about" :about]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:div "Redleg & BELs konsent app" #_{:src "/img/warning_clojure.png"}]])

(defn my-konsents-page []
  [:section.section>div.container>div.content
   [:div "here will be all your contents to choose one"]
   #_(when-let [docs @(rf/subscribe [:docs])]
       [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}])])

(defn home-page []
      [:section.section>div.container>div.content
       ;[:h3 "Your Comments, Discussions and Feature-Requests and more..."]
       [:div.field [:button.button.is-inactive {:disabled true} "the shortes tutorial of the world: "] [d/konsent]]
       [d/discussion-form]
       #_(when-let [docs @(rf/subscribe [:docs])]
           [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}])])

(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div
     [navbar]
     [:div.columns.is-centered>div.column
      [page]]]))

(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/about" {:name :about
                :view #'about-page}]
     ["/my-konsents" {:name :my-konsents
                      :view #'my-konsents-page}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------
;; Initialize app
(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))


(defn init! []
  (println "GOING TO START BELs Konsent Server")
  (start-router!)
  (ajax/load-interceptors!)
  (rf/dispatch-sync [:initialize])
  (mount-components))

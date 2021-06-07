(ns decision-konsent.konsent-events
  (:require [decision-konsent.ajax :as ajax]
            [re-frame.core :as rf]
            [decision-konsent.konsent-fsm :as k-fsm]
            [decision-konsent.unix-time :as ut]))


(rf/reg-sub
  :konsent/list
  (fn [db _]
    (:konsent/list db)))


(rf/reg-sub
  :konsent/active
  (fn [db _]
    (:konsent/active db)))


(rf/reg-event-fx
  :konsent/load-list
  (fn [{:keys [db] :as cofx} [_ _]]
    (let [fields (-> {} (assoc :email (-> db :auth/user :email)))]
      ;(println "\n\nload list: " fields)
      {:http-xhrio (ajax/http-xhrio-get
                     {:uri        "/api/konsent/all-konsents-for-user"
                      :params     fields
                      :on-success [:konsent/handle-load-list]
                      :on-failure [:common/set-error-from-ajax]})})))


(rf/reg-event-db
  :konsent/handle-load-list
  (fn [db [_ data]]
    ;(println "\n\nget list: " data)
    (assoc db :konsent/list data)))


(rf/reg-event-fx
  :konsent/create
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [f       (-> fields
                      (assoc :user (-> db :auth/user :email))
                      (assoc :participants
                             (decision-konsent.utils/emails-split
                               (:participants fields))))
          konsent (k-fsm/create-konsent (:user f)
                                        (:short-name f)
                                        (:problem-statement f)
                                        (:participants f))]
      ;(println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/http-xhrio-post
                     {:uri        "/api/konsent/create-konsent"
                      :params     (:konsent konsent)
                      :on-success [:konsent/activate]
                      :on-failure [:common/set-error-from-ajax]})})))
;:dispatch-n [[:konsent/load-list]]})))


(rf/reg-event-fx
  :konsent/discuss
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [active-k (db :konsent/active)
          user     (-> db :auth/user :email)
          text     (fields :text)
          new-k    (k-fsm/discuss active-k user text)]
      (println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/http-xhrio-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error-from-ajax]})
       :db         (assoc db :konsent/active new-k)})))
;:dispatch-n [[:konsent/load-list]]})))


(rf/reg-event-fx
  :konsent/propose
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [active-k (db :konsent/active)
          user     (-> db :auth/user :email)
          text     (fields :text)
          new-k    (k-fsm/propose active-k user text)]
      (println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/http-xhrio-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error-from-ajax]})
       :db         (assoc db :konsent/active new-k)})))

(rf/reg-event-fx
  :konsent/ask
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [active-k (db :konsent/active)
          user     (-> db :auth/user :email)
          text     (fields :text)
          new-k    (k-fsm/ask active-k user text)]
      (println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/http-xhrio-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error-from-ajax]})
       :db         (assoc db :konsent/active new-k)})))


(rf/reg-event-fx
  :konsent/vote
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [active-k (db :konsent/active)
          user     (-> db :auth/user :email)
          text     (fields :text)
          vote     (fields :vote)
          new-k    (k-fsm/vote active-k user vote text)]
      (println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/http-xhrio-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error-from-ajax]})
       :db         (assoc db :konsent/active new-k)})))




(rf/reg-event-fx
  :konsent/answer
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [active-k (db :konsent/active)
          user     (-> db :auth/user :email)
          text     (fields :text)
          idx      (fields :idx)
          new-k    (k-fsm/answer active-k user text idx)]
      (println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/http-xhrio-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error-from-ajax]})
       :db         (assoc db :konsent/active new-k)})))



(rf/reg-event-fx
  :konsent/ready-to-vote
  (fn [{:keys [db] :as cofx} [_ _]]
    (let [active-k (db :konsent/active)
          user     (-> db :auth/user :email)
          ;text     (fields :text)
          new-k    (k-fsm/signal-ready-to-vote active-k user)]
      ;(println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/http-xhrio-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error-from-ajax]})
       :db         (assoc db :konsent/active new-k)})))



(rf/reg-event-fx
  :konsent/force-vote
  (fn [{:keys [db] :as cofx} [_ _]]
    (let [active-k (db :konsent/active)
          user     (-> db :auth/user :email)
          ;text     (fields :text)
          new-k    (k-fsm/force-vote active-k user)]
      ;(println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/http-xhrio-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error-from-ajax]})
       :db         (assoc db :konsent/active new-k)})))


(rf/reg-event-fx
  :konsent/force-incomplete-vote
  (fn [{:keys [db] :as cofx} [_ _]]
    (let [active-k (db :konsent/active)
          user     (-> db :auth/user :email)
          ;text     (fields :text)
          new-k    (k-fsm/force-incomplete-vote active-k user)]
      ;(println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/http-xhrio-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error-from-ajax]})
       :db         (assoc db :konsent/active new-k)})))


(defn active-konsent-to-list [db]
  (let [active-konsent (db :konsent/active)
        konsent-list   (db :konsent/list)
        list-without   (vec (filter #(not= (:id %) (:id active-konsent)) konsent-list))
        new-db         (assoc db :konsent/list (conj list-without active-konsent))]
    ;(println "\n\nactive:\n")
    ;(cljs.pprint/pprint active-konsent)
    ;(println "\n\nlist-without:\n")
    ;(cljs.pprint/pprint list-without)
    ;(println "\n\nnew db:\n")
    ;(cljs.pprint/pprint new-db)
    new-db))



(rf/reg-event-db
  :konsent/handle-save-konsent
  (fn [db [_ data]]
    (println "\n\nhandle-save-konsent: " data)
    (active-konsent-to-list db)))

(rf/reg-event-fx
  :konsent/delete
  (fn [{:keys [db] :as cofx} [_ fields]]
    (println fields)
    {:http-xhrio (ajax/http-xhrio-post
                   {:uri        "/api/konsent/delete-konsent"
                    :params     fields
                    :on-success [:konsent/load-list]
                    :on-failure [:common/set-error-from-ajax]})}))


(rf/reg-event-fx
  :konsent/activate
  (fn [{:keys [db] :as cofx} [_ data]]
    {:db         (assoc db :konsent/active data)
     :dispatch-n [[:common/navigate! :my-konsents nil nil] [:konsent/load-list]]}))


(rf/reg-event-db
  :konsent/de-activate
  (fn [db [_ data]]
    (dissoc db :konsent/active)))
(ns decision-konsent.konsent-events
  (:require [decision-konsent.ajax :as ajax]
            [re-frame.core :as rf]
            [decision-konsent.konsent-fsm :as k-fsm]
            [decision-konsent.unix-time :as ut]
            [clojure.pprint :as pp]))
;[clojure.tools.logging :as log]))


(rf/reg-sub
  :konsent/active-id
  (fn [db _]
    (:konsent/active-id db)))

(rf/reg-sub
  :konsent/list
  (fn [db _]
    (vals (:konsent/list db))))

(def data {29 {:id 29, :konsent {:owner             "a",
                                 :timestamp         1624266109714,
                                 :iterations        [{:q&a               [],
                                                      :ready             [],
                                                      :votes             [],
                                                      :discussion        [],
                                                      :allowed-proposers ["a"]}],
                                 :short-name        "a",
                                 :participants      ["a"],
                                 :problem-statement nil}}

           28 {:id 28, :konsent {:owner             "a",
                                 :timestamp         1624177944365,
                                 :iterations        [{:q&a               [],
                                                      :ready             [],
                                                      :votes             [],
                                                      :discussion        [],
                                                      :allowed-proposers ["a" "b" "c"]}],
                                 :short-name        "a b c",
                                 :participants      ["a" "b" "c"],
                                 :problem-statement nil}}})

(defn get-active-konsent [k-list k-id]
  ;(println "\nk-list\n" k-list "\nk-id\n" k-id)
  (first (filter #(= (str k-id) (str (:id %))) k-list)))

(defn get-active-konsent-from-db [db]
  (get-active-konsent @(rf/subscribe [ :konsent/list ]) (:konsent/active-id db)))


(rf/reg-sub
  :konsent/active
  :<- [:konsent/list]
  :<- [:konsent/active-id]
  (fn [[k-list k-id] _]
    (get-active-konsent k-list k-id)))


(rf/reg-event-fx
  :konsent/load-list
  (fn [{:keys [db] :as cofx} [_ _]]
    (let [fields (-> {} (assoc :email (-> db :auth/user :email)))]
      ;(println "\n:konsent/load-list " fields)
      {:http-xhrio (ajax/wrap-get
                     {:uri        "/api/konsent/all-konsents-for-user"
                      :params     fields
                      :on-success [:konsent/handle-load-list]
                      :on-failure [:common/set-error]})})))




(rf/reg-event-db
  :konsent/handle-load-list
  (fn [db [_ data]]
    ;(println "\n\nget list: ")
    ;(pp/pprint data)
    (let [id-list (map (fn [k] [(:id k) k]) data)
          ;_       (println "\n\n id-list")
          ;_       (pp/pprint id-list)
          id-map  (into {} id-list)]
      ;_       (println "\n\n id-map")
      ;_       (pp/pprint id-map)]
      (assoc db :konsent/list id-map))))


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
      {:http-xhrio (ajax/wrap-post
                     {:uri        "/api/konsent/create-konsent"
                      :params     (:konsent konsent)
                      :on-success [:konsent/activate]
                      :on-failure [:common/set-error]})})))
;:dispatch-n [[:konsent/load-list]]})))


(rf/reg-event-fx
  :konsent/discuss
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [active-k (get-active-konsent-from-db db)
          ;-        (println active-k)
          ;_        (cljs.pprint/pprint active-k)
          user     (-> db :auth/user :email)
          text     (fields :text)
          new-k    (k-fsm/discuss active-k user text)]
      (println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/wrap-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error]})
       :db         (assoc db :konsent/active new-k)})))
;:dispatch-n [[:konsent/load-list]]})))


(rf/reg-event-fx
  :konsent/propose
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [active-k (get-active-konsent-from-db db)
          user     (-> db :auth/user :email)
          text     (fields :text)
          new-k    (k-fsm/propose active-k user text)]
      ;(println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/wrap-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error]})
       :db         (assoc db :konsent/active new-k)})))

(rf/reg-event-fx
  :konsent/ask
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [active-k (get-active-konsent-from-db db)
          user     (-> db :auth/user :email)
          text     (fields :text)
          new-k    (k-fsm/ask active-k user text)]
      ;(println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/wrap-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error]})
       :db         (assoc db :konsent/active new-k)})))


(rf/reg-event-fx
  :konsent/vote
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [active-k (get-active-konsent-from-db db)
          user     (-> db :auth/user :email)
          text     (fields :text)
          vote     (fields :vote)
          new-k    (k-fsm/vote active-k user vote text)]
      ;(println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/wrap-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error]})
       :db         (assoc db :konsent/active new-k)})))




(rf/reg-event-fx
  :konsent/answer
  (fn [{:keys [db] :as cofx} [_ fields]]
    (let [active-k (get-active-konsent-from-db db)
          user     (-> db :auth/user :email)
          text     (fields :text)
          idx      (fields :idx)
          new-k    (k-fsm/answer active-k user text idx)]
      ;(println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/wrap-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error]})
       :db         (assoc db :konsent/active new-k)})))



(rf/reg-event-fx
  :konsent/ready-to-vote
  (fn [{:keys [db] :as cofx} [_ _]]
    (let [active-k (get-active-konsent-from-db db)
          user     (-> db :auth/user :email)
          ;text     (fields :text)
          new-k    (k-fsm/signal-ready-to-vote active-k user)]
      ;(println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/wrap-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error]})
       :db         (assoc db :konsent/active new-k)})))



(rf/reg-event-fx
  :konsent/force-vote
  (fn [{:keys [db] :as cofx} [_ _]]
    (let [active-k (get-active-konsent-from-db db)
          user     (-> db :auth/user :email)
          ;text     (fields :text)
          new-k    (k-fsm/force-vote active-k user)]
      ;(println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/wrap-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error]})
       :db         (assoc db :konsent/active new-k)})))


(rf/reg-event-fx
  :konsent/force-incomplete-vote
  (fn [{:keys [db] :as cofx} [_ _]]
    (let [active-k (get-active-konsent-from-db db)
          user     (-> db :auth/user :email)
          ;text     (fields :text)
          new-k    (k-fsm/force-incomplete-vote active-k user)]
      ;(println fields)
      ;(println "created konsent cljc: " (k-fsm/create-konsent "" "" "" []))
      {:http-xhrio (ajax/wrap-post
                     {:uri        "/api/konsent/save-konsent"
                      :params     new-k
                      :on-success [:konsent/handle-save-konsent]
                      :on-failure [:common/set-error]})
       :db         (assoc db :konsent/active new-k)})))


#_(defn active-konsent-to-list [db]
    (let [active-konsent (get-active-konsent-from-db db)
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
  (fn [db [_ data]]))
   ;(println "\n\nhandle-save-konsent: " data)))
;(active-konsent-to-list db)))

(rf/reg-event-fx
  :konsent/delete
  (fn [{:keys [db] :as cofx} [_ fields]]
    ;(println fields)
    {:http-xhrio (ajax/wrap-post
                   {:uri        "/api/konsent/delete-konsent"
                    :params     fields
                    :on-success [:konsent/load-list]
                    :on-failure [:common/set-error]})}))


(rf/reg-event-fx
  :konsent/activate
  (fn [{:keys [db] :as cofx} [_ data]]
    (println ":konsent/activate: " (:id data))
    {:db         (assoc db :konsent/active-id (:id data))
     :dispatch-n [ [:konsent/load-list] [:common/navigate! :my-konsents nil nil]]}))


(rf/reg-event-db
  :konsent/de-activate
  (fn [db [_ data]]
    (dissoc db :konsent/active-id)))
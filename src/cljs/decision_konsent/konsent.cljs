(ns decision-konsent.konsent
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [decision-konsent.utils :as utils]
            [decision-konsent.konsent-events]
            [decision-konsent.modal :as m]
            [decision-konsent.bulma-examples :as e]
            [decision-konsent.client-time :as ct]
            [decision-konsent.konsent-fsm :as k-fsm]))

(def login-to-start "first login.\nthen start.")
(def not-logged-in {:data-tooltip login-to-start
                    :class        [:has-tooltip-warning :has-tooltip-active :has-tooltip-arrow]
                    :disabled     true})
(def divider [:div.is-divider.mt-6.mb-6 {:data-content "OR"}])


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
       ;divider])))
       ;[e/all-examples]])))


(defn konsent-example-list []
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
         {:on-click     #(rf/dispatch [:konsent/delete konsent-info])
          :data-tooltip "delete?"
          :class        [:has-tooltip-warning :has-tooltip-arrow]}
         [:i.fas.fa-trash]]
        [:span {:on-click #(rf/dispatch [:konsent/activate konsent-info])}
         (get-in konsent-info [:konsent :short-name])]])]))


(defn konsent-list-page []
  [:nav.panel
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
     [konsent-example-list])])


;;
;; --------------------------- view the active konsent -----------------------------------------------------------------
;;



(defn  votes [iteration]
  [:div.columns
   [:div.column.is-3 "votes:"]
   [:div.column.is-9

    (for [v (-> iteration :votes)]
      [:div.card>div.card-content>div.content
       [:div.columns
        [:div.column.is-2 (:participant v)]
        [:div.column.is-2 (:vote v)]
        [:div.column.is-5 (:text v)]]])]])


(defn accepted [k u]
 [:<>
  [:div.columns
   [:div.column.is-3 "decision taken:"]
   [:div.column.is-9
    [:div.card>div.card-content>div.content  (-> (k-fsm/active-iteration k) :proposal :text)]]]])
  ;[votes (k-fsm/active-iteration k)]])


(defn big-icon-button [callback tooltip-text icon-name]
  [:div.control>button.button.is-primary.is-outlined
   (into {:type "button" :data-tooltip tooltip-text :class "has-tooltip-arrow"} callback)
   [:span.icon>i.fas.fa-1x {:class icon-name}]])


(defn tutorial-buttons [fields]
  [:div.field.has-addons
   #_[:div.control
      [:input.input {:type "text" :placeholder "your concerns here..."}]]
   [big-icon-button
    {:on-click #(do (println "yes") (rf/dispatch [:konsent/vote (into @fields {:vote :yes})]) (reset! fields {}))}
    "No concern.\nLet's do it." "fa-arrow-alt-circle-up"]
   [big-icon-button
    {:on-click #(do (println "minor") (rf/dispatch [:konsent/vote (into @fields {:vote :minor-concern})]) (reset! fields {}))}
    "Minor concern!\nHave to say them.\nBut then: Let's do it." "fa-arrow-alt-circle-right"]
   [big-icon-button
    {:on-click #(do (println "major") (rf/dispatch [:konsent/vote (into @fields {:vote :major-concern})]) (reset! fields {}))}
    "Major concern!\nThey need to be reflected.\nOnly if they are reflected,\nI could go with
    this suggestion." "fa-arrow-alt-circle-down"]
   [big-icon-button
    {:on-click #(do (println "veto") (rf/dispatch [:konsent/vote (into @fields {:vote :veto})]) (reset! fields {}))}
    "VETO.\nI would like to take responsibility\nand make a next suggestion.\nYou should consider:
    This is really ultima ratio..." "fa-bolt"]
   [big-icon-button
    {:on-click #(do (println "abstain") (rf/dispatch [:konsent/vote (into @fields {:vote :abstain})]) (reset! fields {}))}
    "Abstain from voting this time.\nI can live with whatever will be decided." "fa-comment-slash"]])


(defn vote-for-konsent [k u]
  (let [fields (r/atom {:text ""})]
    (fn []
      [:div.columns
       [:div.column.is-3 "my vote"]
       [:div.column.is-9
        [:form.box utils/prevent-reload
         [:div.field>div.control>textarea.textarea
          {:placeholder "Your comment here...\nminor-concern - please hear me.\nmajor-concern - needs be built into proposal.\nveto - I will do next proposal."
           :value       (:text @fields)
           :on-change   #(swap! fields assoc :text (-> % .-target .-value))}]
         [:div.field [tutorial-buttons fields]
          #_{:on-click #(do (rf/dispatch [:konsent/ask @fields]) (reset! fields {}))}]]]]))
  ;)}

  #_[:div.columns
     [:div.column.is-3 "my vote"]
     [:div.column
      [decision-konsent.home/tutorial-buttons]]])


(defn time-gone-by [t]
  (let [localtime   @(rf/subscribe [:timestamp])
        server-diff @(rf/subscribe [:server-diff-time])
        gone-by     (ct/human-duration-sd t localtime server-diff)]
    ;(println "t: " t)
    ;(println "gone-by: " gone-by)
    ;(println "local: " localtime)
    [:small (str "~ " gone-by)]))


(defn add-time-badge [t]
  [:span.badge.is-info [time-gone-by t]])

(defn add-time-user-badge [t u]
  [:span.badge.is-info u " " [time-gone-by t]])


(defn owner-started [o t]
  [:div
   [:div.buttons.are-small
    [:button.button.is-outlined.is-rounded {:key o} [:span.badge.is-info [time-gone-by t]] o]]])


(defn participants [ps]
  [:div.columns
   [:div.column.is-3 "participants:"]
   [:div.column
    [:div.buttons.are-small
     (for [p ps]
       [:button.button.is-outlined.is-rounded {:key p} p])]]])

(defn created-by-when [owner timestamp]
  [:div.columns
   [:div.column.is-3 "created by: "]
   [:div.column [owner-started owner timestamp]]])

(defn short-name-and-problemstatement [sn ps]
  [:div.columns
   [:div.column.is-3 [:h1.title.is-5 sn]]
   [:div.column ps]])

(defn participant-list [ps]
  [:div.buttons.are-small
   (for [p ps]
     [:button.button.is-outlined.is-rounded {:key p} p])])

(defn wait-for-everybody-voted [k u]
  [:div.columns
   [:div.column.is-3 "Waiting for votes of:"]
   [:div.column.is-9
    [:div.card>div.card-content>div.content [participant-list (k-fsm/users-with-missing-votes k)]]]])


; TODO rename to force-incomplete-vote
(defn force-vote [k u]
  [:div.columns
   [:div.column.is-3 "force end of voting?"]
   [:div.column.is-9
    [:div.card>div.card-content>div.content [participant-list (k-fsm/users-with-missing-votes k)]]
    [:form.box utils/prevent-reload
     [:div.field>div.control
      [:label.checkbox.mr-3
       [:input {:type     "checkbox"
                :on-click #(rf/dispatch [:konsent/force-incomplete-vote])}] " force end?"]]]]])


; TODO: rename to force-all-ready
(defn force-ready [k u]
  [:div.columns
   [:div.column.is-3 "force end of asking?"]
   [:div.column.is-9
    [:form.box utils/prevent-reload
     [:div.field>div.control.mr-3
      [:label.checkbox [:input {:type     "checkbox"
                                :on-click #(rf/dispatch [:konsent/force-vote])}] " force vote?"]]]]])


(defn wait-for-ready [k u]
  [:div.columns
   [:div.column.is-3 "waiting for ready to vote:"]
   [:div.column.is-9
    [:div.card>div.card-content>div.content [participant-list (k-fsm/users-missing-for-ready-to-vote k)]]]])


(defn unanswered-question [q]
  (let [fields (r/atom {:text ""})
        ts     (-> q :question :timestamp)
        p      (-> q :question :participant)
        idx    (-> q :idx)]
    (fn []
      [:div.columns {:key idx}
       [:div.column.is-3 "Please answer:"]
       [:div.column.is-9
        [:form.box utils/prevent-reload
         [:div.card>div.card-content>div.content
          [add-time-user-badge ts p]
          (-> q :question :text)]
         [:div.field>div.control>textarea.textarea
          {:placeholder "Answer the question above..."
           :value       (:text @fields)
           :on-change   #(swap! fields assoc :text (-> % .-target .-value))}]
         [:div.field>div.control>button.button
          {:on-click #(do (rf/dispatch [:konsent/answer (assoc @fields :idx idx)]) (reset! fields {}))}
          ;)}
          "answer"]]]])))


(defn answer [k]
  (let [unanswered (seq (reverse (k-fsm/q&a-without-answers k)))]
    (if unanswered
      [:div
       (for [q unanswered]
         [unanswered-question (into q {:key (:idx q)})])]
      [:div.columns
       [:div.column.is-3 "questions from other members?"]
       [:div.column.is-9 "no unanswered questions..."]])))



(defn ask [k]
  (let [fields (r/atom {:text ""})]
    (fn []
      [:div.columns
       [:div.column.is-3 "ask another question or signal ready..."]
       [:div.column.is-9

        [:form.box utils/prevent-reload
         [:div "users ready to vote: " [participant-list (k-fsm/users-ready-to-vote k)]]
         [:div.field>div.control
          [:label.checkbox [:input {:type     "checkbox"
                                    :on-click #(rf/dispatch [:konsent/ready-to-vote])}] " ready to vote!"]]

         [:div.field>div.control>textarea.textarea
          {:placeholder "I'm not yet done... My question is...ENTER"
           :value       (:text @fields)
           :on-change   #(swap! fields assoc :text (-> % .-target .-value))}]
         [:div.field>div.control>button.button
          {:on-click #(do (rf/dispatch [:konsent/ask @fields]) (reset! fields {}))}
          ;)}
          "ask"]]]])))


(defn discuss []
  (let [fields (r/atom {:text ""})]
    (fn []
      [:div.columns
       [:div.column.is-3 "discuss:"]
       [:div.column
        [:form.box utils/prevent-reload
         [:div.field>div.control>textarea.textarea
          {:placeholder "discuss perspectives, options, questions..."
           :value       (:text @fields)
           :on-change   #(swap! fields assoc :text (-> % .-target .-value))}]
         [:div.field>div.control>button.button
          {:on-click #(do (rf/dispatch [:konsent/discuss @fields]) (reset! fields {}))}
          "send"]]]])))


(defn propose []
  (let [fields (r/atom {})]
    (fn []
      [:div.columns
       [:div.column.is-3 "propose:"]
       [:div.column
        [:form.box utils/prevent-reload
         [:div.field>div.control>textarea.textarea
          {:placeholder "create a proposal for voting"
           :on-change   #(swap! fields assoc :text (-> % .-target .-value))}]
         [:div.field>div.control>button.button
          {:on-click #(rf/dispatch [:konsent/propose @fields])}
          "create proposal"]]]])))



(defn one-iteration [iteration]
  (print "iteration: " (cljs.pprint/pprint iteration))
  (let [votes-in-iter            (:votes iteration)
        ;active-iteration (k-fsm/active-iteration k)
        messages         (reverse (:discussion iteration))
        proposal         (:proposal iteration)
        q&a              (seq (reverse (:q&a iteration)))] ;TODO do that all by subscriptions
    [:div
     ;(when ())
     (when votes-in-iter
      [votes iteration])
     (when q&a
       [:div.columns
        [:div.column.is-3 "q&a after proposal"]
        [:div.column
         (for [one-q&a q&a]
           (let [q   (:question one-q&a)
                 a   (:answer one-q&a)
                 qts (:timestamp q)
                 ats (:timestamp a)
                 qp  (:participant q)
                 ap  (:participant a)
                 idx (:idx one-q&a)]
             ;[:div.box {:key (:timestamp message)}]
             [:div.columns {:key qts}
              [:div.column
               [:div.card>div.card-content>div.content
                [add-time-user-badge qts qp]
                (:text q)]
               (if a [:div.card>div.card-content>div.content
                      [add-time-user-badge ats ap]
                      (:text a)]
                     [:div.card>div.card-content>div.content "no answer yet"])]]))]])
     (when proposal
       [:div.columns
        [:div.column.is-3 [:h1.title.is-5 "proposal:"]]
        [:div.column
         [:div.card>div.card-content>div.content
          [add-time-user-badge (:timestamp proposal) (:participant proposal)]
          (:text proposal)]]])
     [:div.columns
      [:div.column.is-3 "discussion before proposal:"]
      [:div.column.is-9
       ;[:div.columns
       (for [message messages]
         ;[:div.box {:key (:timestamp message)}]
         [:div.columns {:key (:timestamp message)}
          [:div.column
           [:div.card>div.card-content>div.content
            [add-time-user-badge (:timestamp message) (:participant message)]
            (:text message)]]])]]]))


#_(defn one-iteration [iteration]
    (let [
          votes    (:votes iteration)
          proposal (:proposal iteration)
          q&a      (seq (reverse (k-fsm/q&a-with-idx iteration)))
          messages (reverse (:discussion iteration))]))

;TODO do that all by subscriptions

(defn history [k]
  ; (println (-> k :konsent :iterations))
  ;(println "history:  " (-> k :konsent :iterations))
  #_(let [iter-with-idx (map (fn [iter key] {:key key :iter iter}) (-> k :konsent :iterations) (range))]
     [:<>
      (for [i iter-with-idx]
        ^{:key (:key i)}
        [one-iteration (:iter i)])]))



(defn debugging [k u]
 [:div
  [:div.is-divider.mt-6.mb-6 {:data-content "DEBUGGING"}]
  [:div.columns
   [:div.column.is-1 "DBG"]
   [:div.column.is-11
    ;[:pre (str "wait for = " (k-fsm/wait-for-other-users-to-be-ready k u))]
    [:pre (str "user = " u)]
    [:pre (str "next-actions-all-users     = " (k-fsm/next-actions-all-user k))]
    ;[:pre (str "all-signaled-ready         = " (k-fsm/all-signaled-ready? k))]
    ;[:pre (str "proposer                   = " (k-fsm/proposer k))]
    ;[:pre (str "proposal-made?             = " (k-fsm/proposal-made? k))]
    ;[:pre (str "allowed-proposers          = " (k-fsm/allowed-proposers k))]
    ;[:pre (str "allowed-to-propose?      u = " (k-fsm/allowed-to-propose? k u))]
    ;[:pre (str "user-is-proposer?        u = " (k-fsm/user-is-proposer? k u))]
    ;[:pre (str "votes-set                  = " (k-fsm/votes-set k))]
    ;[:pre (str "voters-set                 = " (k-fsm/voters-set k))]
    ;[:pre (str "veto-voters                = " (k-fsm/veto-voters k))]
    ;[:pre (str "user-ready-to-vote?      u = " (k-fsm/user-ready-to-vote? k u))]
    ;[:pre (str "user-voted?              u = " (k-fsm/user-voted? k u))]
    ;[:pre (str "voting-over?               = " (k-fsm/voting-over? k))]
    ;[:pre (str "veto?                      = " (k-fsm/veto? k))]
    ;[:pre (str "major?                     = " (k-fsm/major? k))]
    ;[:pre (str ":ready               = " (-> (k-fsm/active-iteration k) :ready))]
    ;[:pre (str "all-members        = " (k-fsm/all-members k))]
    ;[:pre (str "all-mebers = :ready         = " (= (-> (k-fsm/active-iteration k) :ready)
    ;                                               (set (k-fsm/all-members k))))]
    ;[:pre (str "all-signaled-ready           = " (k-fsm/all-signaled-ready? k))]
    [:pre (str " ======================================= ")]
    [:pre (str "accepted?                  = " (k-fsm/accepted? k))]
    [:pre (str "voting-started?            = " (k-fsm/voting-started? k))]
    [:pre (str "incomplete-voting-ongoing? = " (k-fsm/incomplete-voting-ongoing? k))]
    [:pre (str "wait-for-other-voters?    u = " (k-fsm/wait-for-other-voters? k u))]
    [:pre (str "next-iteration?            = " (k-fsm/next-iteration? k))]
    [:pre (str "vote?                     u = " (k-fsm/vote? k u))]
    [:pre (str "force-incomplete-vote?    u = " (k-fsm/force-incomplete-vote? k u))]
    [:pre (str "ask?                      u = " (k-fsm/ask? k u))]
    [:pre (str "answer?                   u = " (k-fsm/answer? k u))]
    [:pre (str "discuss?                  u = " (k-fsm/discuss? k u))]
    [:pre (str "propose?                  u = " (k-fsm/propose? k u))]
    [:pre (str " ======================================= ")]
    [:pre (str "users-with-missing-votes           = " (k-fsm/users-with-missing-votes k))]
    [:pre (str "users-missing-for-ready-to-vote    = " (k-fsm/users-missing-for-ready-to-vote k))]
    [:pre (str "wait-for-other-users-to-be-ready u = " (k-fsm/wait-for-other-users-to-be-ready k u))]


    ;[:pre (str "(not (k-fsm/user-voted? k u)) = " (not (k-fsm/user-voted? k u)))] ; (not (k-fsm/user-voted? k u))
    [:pre (str "konsent = " (with-out-str (cljs.pprint/pprint k)))]]]])

(defn konsent-active []
  #_[:a.panel-block (str @(rf/subscribe [:konsent/active]))]
  (let [k     @(rf/subscribe [:konsent/active])
        u     @(rf/subscribe [:auth/user])
        ;k            (:konsent konsent-info)
        sn    (-> k :konsent :short-name)
        ps    (-> k :konsent :participants)
        probs (-> k :konsent :problem-statement)]
    [:div
     [:div.columns
      [:div.column.is-3
       [:button.button.is-primary
        (if u
          {:on-click #(rf/dispatch [:konsent/de-activate])}
          not-logged-in)
        "back to all konsents"]]
      [:div.column.is-9 ""]]
     (if u
       [:div
        ;[short-name-and-problemstatement sn probs]
        ;[:div.is-divider.mt-6.mb-6 {:data-content "ask a question only to understand proposal"}]
        ;[ask]
        ;[:div.is-divider.mt-6.mb-6 {:data-content "free discussion to learn views, feelings, facts, opinions"}]
        (when (k-fsm/accepted? k)
          [:div
           [:div.is-divider.mt-6.mb-6 {:data-content "Proposal ACCEPTED"}]
           [accepted k u]
           #_(when (k-fsm/user-is-proposer? k u)
               [force-vote k u])])

        (when (and (k-fsm/voting-started? k) (k-fsm/user-voted? k u) (not (k-fsm/user-is-proposer? k u)) (not (k-fsm/accepted? k)))
          [:div
           [:div.is-divider.mt-6.mb-6 {:data-content "waiting for others to vote"}]
           [wait-for-everybody-voted k u]
           #_(when (k-fsm/user-is-proposer? k u)
               [force-vote k u])])

        (when (and (k-fsm/voting-started? k) (k-fsm/user-is-proposer? k u) (not (k-fsm/accepted? k)))
          [:div
           [:div.is-divider.mt-6.mb-6 {:data-content "force vote?"}]
           [force-vote k u]])

        (when (and (k-fsm/voting-started? k) (not (k-fsm/user-voted? k u)) (not (k-fsm/accepted? k)))
          [:div
           [:div.is-divider.mt-6.mb-6 {:data-content "voting time..."}]
           [vote-for-konsent k u]
           [wait-for-everybody-voted k u]])

        (when (and (k-fsm/wait-for-other-users-to-be-ready k u)
                   (k-fsm/user-is-proposer? k u)
                   (not (k-fsm/voting-started? k)))
          [:div
           [:div.is-divider.mt-6.mb-6 {:data-content "go ahead?"}]
           [wait-for-ready k u]
           [force-ready k u]])
        (when (and (k-fsm/wait-for-other-users-to-be-ready k u)
                   (not (k-fsm/user-is-proposer? k u))
                   (not (k-fsm/voting-started? k)))
          [:div
           [:div.is-divider.mt-6.mb-6 {:data-content "please wait"}]
           [wait-for-ready k u]])
        (when (k-fsm/ask? k u)
          [:div
           [:div.is-divider.mt-6.mb-6 {:data-content "understand proposal"}]
           [ask k]])
        (when (k-fsm/answer? k u)
          [:div
           [:div.is-divider.mt-6.mb-6 {:data-content "answer questions regarding your proposal"}]
           [answer k]])
        (when (k-fsm/propose? k u)
          [:div
           [:div.is-divider.mt-6.mb-6 {:data-content "create proposal"}]
           [propose]])
        (when (k-fsm/discuss? k u)
          [:div
           [:div.is-divider.mt-6.mb-6 {:data-content "free discussion to learn views, feelings, facts, opinions"}]
           [discuss]])
        ;[:div.is-divider.mt-6.mb-6 {:data-content "or start a konsent by suggesting"}]
        ;[vote-for-konsent]
        ;[:div.is-divider.mt-6.mb-6 {:data-content "start konsent"}]
        ;[start-konsent]
        [:div.is-divider.mt-6.mb-6 {:data-content "what happend before..."}]
        [history k]
        [:div.is-divider.mt-6.mb-6 {:data-content "started by with participants"}]
        [short-name-and-problemstatement sn probs]
        [participants ps]
        [created-by-when (-> k :konsent :owner) (-> k :konsent :timestamp)]]
        ;[debugging k u]]

       [konsent-example-list])]))






(defn my-konsents-page []
  [:section.section>div.container>div.content
   (if @(rf/subscribe [:konsent/active])
     [konsent-active]
     [konsent-list-page])])

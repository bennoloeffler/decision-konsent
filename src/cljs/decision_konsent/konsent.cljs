(ns decision-konsent.konsent
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [decision-konsent.utils :as utils]
            [decision-konsent.konsent-events]
            [decision-konsent.modal :as m]
            [decision-konsent.bulma-examples :as e]
            [decision-konsent.client-time :as ct]
            [decision-konsent.konsent-fsm :as k-fsm]
            [decision-konsent.i18n :refer [tr]]))


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


(defn not-logged-in [] {:data-tooltip (tr [:k/login-then-start])
                        :class        [:has-tooltip-warning :has-tooltip-active :has-tooltip-arrow]
                        :disabled     true})


(def divider [:div.is-divider.mt-6.mb-6 {:data-content (tr [:k/or-divider])}])


(defn value [element]
  (-> element .-target .-value))


(defn new-konsent-page []
  (let [fields (r/atom {})]
    (fn []
      [:section.section>div.container>div.content
       [:div.field
        [:form.box utils/prevent-reload
         [:h1.title.is-4 (tr [:k/new])]
         [:div.field
          [:label.label (tr [:k/short-name])]
          [:div.control
           [:input.input {:type        "text"
                          :placeholder (tr [:k/inp-coffee-sn] "e.g.    our old and loved coffee machine broke - what now?")
                          :on-change   #(swap! fields assoc :short-name (value %))}]]]
         [:div.field
          [:label.label (tr [:k/problem-description])]
          [:div.control
           [:textarea.textarea {:type        "text"
                                :placeholder (tr [:k/inp-coffee-pd] "e.g.\nIt broke. Repairing seems impossible.\nThere are plenty of options and unlimited prices and even more opinions.\nIts even unclear, if there is a real need for a coffee machine. So we need to take a decision and focus back on real topics.")
                                :on-change   #(swap! fields assoc :problem-statement (value %))}]]]
         [:div.field
          [:label.label (tr [:k/participants-mail])]
          [:div.control
           [:textarea.textarea {:type        "text"
                                :placeholder (tr [:k/inp-coffee-part] "e.g.\nhugo.huelsensack@the-company.com\nbruno.banani@the-same-company.com\nmarc.more@the-company.com\n\nseparated by space, comma, semicolon, return")
                                :rows        "10"
                                :on-change   #(swap! fields assoc :participants (value %))}]]]

         [:div [:button.button.is-primary.mt-3.has-tooltip-right
                (if @(rf/subscribe [:auth/user])
                  {:on-click #(rf/dispatch [:konsent/create @fields])}
                  (not-logged-in))
                [:span.icon.is-large>i.fas.fa-1x.fa-door-open]
                [:div (tr [:k/create-and-start])]]]]]])))

;divider])))
;[e/all-examples]])))


(defn konsent-example-list []
  [:div
   [:a.panel-block
    [:span.panel-icon
     [:i.fas.fa-book {:aria-hidden "true"}]] (tr [:k/example-bib])]
   [:a.panel-block
    [:span.panel-icon
     [:i.fas.fa-bomb {:aria-hidden "true"}]] (tr [:k/example-coffee-machine])]
   [:a.panel-block
    [:span.panel-icon
     [:i.fas.fa-fighter-jet {:aria-hidden "true"}]] (tr [:k/example-travel-rules])]])


(defn konsents-of-user []
  "show all konsents for user based on data [{:id 12 :short-name 'abc' :timestamp :changes} ...]"
  ;(println @(rf/subscribe [:auth/guest?]))
  (let [konsents  @(rf/subscribe [:konsent/list])
        guest?    @(rf/subscribe [:auth/guest?])
        tooltip   (if guest? (tr [:k/guest-not-allowed-to-del]) (tr [:k/delete?]))
        active-id (str @(rf/subscribe [:konsent/active-id]))]
    [:div
     (for [konsent-info konsents]
       [:a.panel-block
        {:key (:id konsent-info)}
        [:span.panel-icon
         {:on-click     #(if guest? (println "not allowed") (rf/dispatch [:konsent/delete konsent-info]))
          :data-tooltip tooltip
          :class        [:has-tooltip-warning :has-tooltip-arrow]}
         [:i.fas.fa-trash]]
        [:span {:style (if (= active-id (str (:id konsent-info))) {:font-weight :bold} {}) :on-click #(rf/dispatch [:common/navigate! :my-konsent {:id (:id konsent-info)}])} ;#(rf/dispatch [:konsent/activate konsent-info])}
         (get-in konsent-info [:konsent :short-name])]])]))


(defn konsent-list-page []
  (let [guest? @(rf/subscribe [:auth/guest?])]

    [:nav.panel
     [:div.panel-heading
      [:nav.level
       ;[:div.column.is-3
       [:button.button.is-primary.mr-5

        (if @(rf/subscribe [:auth/user])
          {:on-click     #(rf/dispatch [:common/navigate! :new-konsent nil nil])
           :disabled     guest?
           :data-tooltip (when guest?
                           (tr [:k/guest-login-to-create-konsent]))

           :class        [:has-tooltip-arrow]}
          (not-logged-in))

        (when guest? [:span.badge.is-info (tr [:k/guest])])
        ;[:div (tr [:k/create])]
        [:span.icon.is-large>i.fas.fa-1x.fa-plus-circle]]

       [:div (tr [:k/all-my-konsents] "all my konsents")]]]
     (if @(rf/subscribe [:auth/user])
       [konsents-of-user]
       [konsent-example-list])]))


;;
;; --------------------------- view the active konsent -----------------------------------------------------------------
;;


(defn my-vote [iteration, user-email]
  [:div.columns
   [:div.column.is-3 (tr [:k/my-vote] "my vote:")]
   [:div.column.is-8
    (doall                                                  ; otherwise - error
      (for [v (-> iteration :votes) :when (= user-email (:participant v))]
        (let [color     (case (:vote v)
                          "yes" :lightgreen
                          "minor-concern" :green
                          "major-concern" :orange
                          "veto" :red
                          :lightgray)
              vote-text (tr [(keyword (str "v/" (:vote v)))])]
          [:div.card {:key (:participant v)}
           [:div.card-content>div.content
            [:div.columns
             [:div.column.is-2 [:span {:style
                                       {:color color}} vote-text]]
             ;[:div.column.is-3 (:participant v)]
             [:div.column>div.card>div.card-content>div.content
              [add-time-user-badge (:timestamp v) (:participant v)] (:text v)]]]])))]])


(defn votes [iteration]
  [:div.columns
   [:div.column.is-3 (tr [:k/votes] "votes:")]
   [:div.column.is-8
    (doall                                                  ; otherwise - error
      (for [v (-> iteration :votes)]
        (let [color     (case (:vote v)
                          "yes" :lightgreen
                          "minor-concern" :green
                          "major-concern" :orange
                          "veto" :red
                          :lightgray)
              vote-text (tr [(keyword (str "v/" (:vote v)))])]
          [:div.card {:key (:participant v)}
           [:div.card-content>div.content
            [:div.columns
             [:div.column.is-2 [:span {:style
                                       {:color color}} vote-text]]
             ;[:div.column.is-3 (:participant v)]
             [:div.column>div.card>div.card-content>div.content
              [add-time-user-badge (:timestamp v) (:participant v)] (:text v)]]]])))]])


(defn accepted [k u]
  [:<>
   [:div.columns
    [:div.column.is-3 [:span {:style {:color :lightgreen :font-weight :bold}} (tr [:k/decision-taken] "decision taken:")]]
    [:div.column.is-8
     [:div.card>div.card-content>div.content (-> (k-fsm/active-iteration k) :proposal :text)]]]])
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
    (tr [:h/too-no-concern] "No concern.\nLet's do it.") "fa-arrow-alt-circle-up"]
   [big-icon-button
    {:on-click #(do (println "minor") (rf/dispatch [:konsent/vote (into @fields {:vote :minor-concern})]) (reset! fields {}))}
    (tr [:h/too-minor] "Minor concern!\nHave to say it.\nBut then: Let's do it.") "fa-arrow-alt-circle-right"]
   [big-icon-button
    {:on-click #(do (println "major") (rf/dispatch [:konsent/vote (into @fields {:vote :major-concern})]) (reset! fields {}))}
    (tr [:h/too-major] "Major concern!\nThey need to be reflected.\nOnly if they are reflected,\nI could go with
     this suggestion.") "fa-arrow-alt-circle-down"]
   [big-icon-button
    {:on-click #(do (println "veto") (rf/dispatch [:konsent/vote (into @fields {:vote :veto})]) (reset! fields {}))}
    (tr [:h/too-veto] "VETO.\nI would like to take responsibility\nand make a next suggestion.\nYou should consider:
     This is really ultima ratio...") "fa-bolt"]
   [big-icon-button
    {:on-click #(do (println "abstain") (rf/dispatch [:konsent/vote (into @fields {:vote :abstain})]) (reset! fields {}))}
    (tr [:h/too-abstain] "Abstain from voting this time.\nI can live with whatever will be decided.") "fa-comment-slash"]])


(defn vote-for-konsent [k u]
  (let [fields (r/atom {:text ""})]
    (fn []
      [:div.columns
       [:div.column.is-3 (tr [:k/my-vote] "my vote")]
       [:div.column.is-8
        [:form.box utils/prevent-reload
         [:div.field>div.control>textarea.textarea
          {:placeholder (tr [:k/pla-vote-comments] "Your comment here...\nminor-concern - please hear me.\nmajor-concern - needs be built into proposal.\nveto - I will do next proposal.")
           :value       (:text @fields)
           :on-change   #(swap! fields assoc :text (value %))}]
         [:div.field [tutorial-buttons fields]
          #_{:on-click #(do (rf/dispatch [:konsent/ask @fields]) (reset! fields {}))}]]]]))
  ;)}

  #_[:div.columns
     [:div.column.is-3 "my vote"]
     [:div.column
      [decision-konsent.home/tutorial-buttons]]])





(defn owner-started [o t]
  [:div
   [:div.buttons.are-small
    [:button.button.is-outlined.is-rounded {:key o} [:span.badge.is-info [time-gone-by t]] o]]])


(defn participants [ps]
  [:div.columns
   [:div.column.is-3 (tr [:k/participants] "participants:")]
   [:div.column
    [:div.buttons.are-small
     (for [p ps]
       [:button.button.is-outlined.is-rounded {:key p} p])]]])

(defn created-by-when [owner timestamp]
  [:div.columns
   [:div.column.is-3 (tr [:k/creator] "created by: ")]
   [:div.column [owner-started owner timestamp]]])

(defn short-name-and-problemstatement [sn ps]
  [:div.columns
   [:div.column.is-3 [:h1.title.is-5 sn]]
   [:div.column ps]])

(defn participant-list [ps]
  [:div.buttons.are-small
   (for [p ps]
     [:button.button.is-outlined.is-rounded {:key p :disabled true} p])])

(defn wait-for-everybody-voted [k u]
  [:div.columns
   [:div.column.is-3 (tr [:k/waiting-for-votes-of] "Waiting for votes of:")]
   [:div.column.is-8
    [:div.card>div.card-content>div.content [participant-list (k-fsm/users-with-missing-votes k)]]]])


; TODO rename to force-incomplete-vote
(defn force-vote [k u]
  [:div.columns
   [:div.column.is-3 (tr [:k/force-end-wait-for] "force end? waiting for:")]
   [:div.column.is-8
    [:div.card>div.card-content>div.content [participant-list (k-fsm/users-with-missing-votes k)]]
    [:form.box utils/prevent-reload
     [:div.field>div.control
      [:button.button
       {:on-click #(rf/dispatch [:konsent/force-incomplete-vote])}
       (tr [:k/btn-force-end-of-voting] "force end of voting?")]]]]])


; TODO: rename to force-all-ready
(defn force-ready [k u]
  [:div.columns
   [:div.column.is-3 (tr [:k/force-end-of-asking] "force end of asking?")]
   [:div.column.is-8
    [:form.box utils/prevent-reload
     [:div.field>div.control.mr-3
      [:button.button {;:type     "checkbox"
                       :on-click #(rf/dispatch [:konsent/force-vote])} (tr [:k/btn-force-vote] "force vote?")]]]]])


(defn wait-for-ready [k u]
  [:div.columns
   [:div.column.is-3 (tr [:k/waiting-for-ready-to-vote] "waiting for ready to vote:")]
   [:div.column.is-8
    [:div.card>div.card-content>div.content [participant-list (k-fsm/users-missing-for-ready-to-vote k)]]]])


(defn unanswered-question [q]
  (let [fields (r/atom {:text ""})
        ts     (-> q :question :timestamp)
        p      (-> q :question :participant)
        idx    (-> q :idx)]
    (fn []
      [:div.columns {:key idx}
       [:div.column.is-3 (tr [:k/please-answer] "Please answer:")]
       [:div.column.is-8
        [:form.box utils/prevent-reload
         [:div.card>div.card-content>div.content
          [add-time-user-badge ts p]
          (-> q :question :text)]
         [:div.field>div.control>textarea.textarea
          {:placeholder "Answer the question above..."
           :value       (:text @fields)
           :on-change   #(swap! fields assoc :text (value %))}]
         [:div.field>div.control>button.button
          {:on-click #(do (rf/dispatch [:konsent/answer (assoc @fields :idx idx)]) (reset! fields {}))}
          ;)}
          (tr [:k/btn-answer] "answer")]]]])))


(defn answer [k]
  (let [unanswered (seq (reverse (k-fsm/q&a-without-answers k)))]
    (if unanswered
      [:div
       (for [q unanswered]
         [unanswered-question (into q {:key (:idx q)})])]
      [:div.columns
       [:div.column.is-3 (tr [:k/questions-from-others] "questions from other members?")]
       [:div.column.is-8 (tr [:k/no-questions] "no unanswered questions...")]])))



(defn ask [k]
  (let [fields (r/atom {:text ""})]
    (fn []
      [:div.columns
       [:div.column.is-3 (tr [:k/ask-or-ready] "ask another question or signal ready...")]
       [:div.column.is-8

        [:form.box utils/prevent-reload
         [:div.buttons.are-small [:div.mr-4 (tr [:k/ready-to-vote-are] "ready to vote are:")] [participant-list (k-fsm/users-ready-to-vote k)]]


         [:div.field>div.control>textarea.textarea
          {:placeholder "I'm not yet done... My question is...ENTER"
           :value       (:text @fields)
           :on-change   #(swap! fields assoc :text (value %))}]
         [:div.field>div.control
          [:div.buttons
           [:button.button
            {:on-click #(do (rf/dispatch [:konsent/ask @fields]) (reset! fields {}))}
            ;)}
            (tr [:k/btn-ask] "ask")]
           [:button.button
            {:on-click #(rf/dispatch [:konsent/ready-to-vote])}
            (tr [:k/btn-no-more-questions] "no more questions - ready to vote!")]]]]]])))


(defn discuss []
  (let [fields (r/atom {:text ""})]
    (fn []
      [:div.columns
       [:div.column.is-3 (tr [:k/discuss] "discuss:")]
       [:div.column.is-8
        [:form.box utils/prevent-reload
         [:div.field>div.control>textarea.textarea
          {:placeholder (tr [:k/pla-discuss-perspectives] "discuss perspectives, options, questions...")
           :value       (:text @fields)
           :on-change   #(swap! fields assoc :text (value %))}]
         [:div.field>div.control>button.button
          {:on-click #(do (rf/dispatch [:konsent/discuss @fields]) (reset! fields {}))}
          (tr [:h/btn-send] "send")]]]])))

(defn discuss-social []
  (let [fields (r/atom {:text ""})]
    (fn []
      [:div.columns
       ;[:div.column.is-3 (tr [:k/discuss] "discuss:")]
       [:div.column.is-11
        ;[:form.box utils/prevent-reload
        ;[:nav.level
        ;[:div.level-left
        ;[:div.level-item
        [:div.field>div.control>input.input.is-rounded
         {:placeholder (tr [:k/pla-discuss-perspectives] "discuss perspectives, options, questions...")
          :value       (:text @fields)
          :on-change   #(swap! fields assoc :text (value %))}]]

       ;[:div.level-item
       [:div.column.is-1
        [:div.field>div.control>button.button.is-rounded
         {:on-click #(when (not-empty (:text @fields))
                       (rf/dispatch [:konsent/discuss @fields])
                       (reset! fields {}))}
         [:span.icon.is-large>i.fas.fa-1x.fa-paper-plane]]]])))


(defn propose []
  (let [fields (r/atom {})]
    (fn []
      [:div.columns
       [:div.column.is-3 (tr [:k/create-proposal] "propose:")]
       [:div.column.is-8
        [:form.box utils/prevent-reload
         [:div.field>div.control>textarea.textarea
          {:placeholder (tr [:k/pla-create-proposal] "create a proposal for voting")
           :on-change   #(swap! fields assoc :text (value %))}]
         [:div.field>div.control>button.button
          {:on-click #(rf/dispatch [:konsent/propose @fields])}
          (tr [:k/btn-send-proposal] "create proposal")]]]])))

(defn propose-social []
  (let [fields (r/atom {})]
    (fn []
      [:div.columns
       ;[:div.column.is-3 (tr [:k/create-proposal] "propose:")]
       [:div.column.is-11
        ;[:form.box utils/prevent-reload
        [:div.field>div.control>input.input.is-rounded
         {:placeholder (tr [:k/pla-create-proposal] "create a proposal for voting")
          :on-change   #(swap! fields assoc :text (value %))}]]
       [:div.column.is-1
        [:div.field>div.control>button.button.is-rounded
         {:on-click #(when (not-empty (:text @fields)) (rf/dispatch [:konsent/propose @fields]))}
         [:span.icon.is-large>i.fas.fa-1x.fa-users]]]])))


(defn voters-set
  "returns the set of all voters of THIS iteration.
   e.g. in order to check, if everybody voted."
  [iteration]
  (->> iteration
       :votes
       (map :participant)
       set))


(defn voting-over?
  "is voting over for iteration - because everybody
   voted or the end of voting is forced?"
  [iteration k]
  (or (= (voters-set iteration)
         (decision-konsent.konsent-fsm/all-members k))
      (iteration :force-incomplete-vote)))


(defn one-iteration [iteration i k]
  ;(print "iteration: " (cljs.pprint/pprint iteration))
  (let [votes-in-iter (:votes iteration)
        ;active-iteration (k-fsm/active-iteration k)
        messages      (reverse (:discussion iteration))
        proposal      (:proposal iteration)
        q&a           (seq (reverse (:q&a iteration)))]     ;TODO do that all by subscriptions
    [:div
     [:div.is-divider.mt-6.mb-6 {:data-content (str (tr [:k/iteration] "Iteration ") i)}]
     ;(when ())
     (when (and votes-in-iter (voting-over? iteration k))
       [votes iteration])
     (when q&a
       [:div.columns
        [:div.column.is-3 (tr [:k/q&a] "q&a after proposal")]
        [:div.column.is-8
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
        [:div.column.is-3 [:h1.title.is-5 (tr [:k/proposal] "proposal:")]]
        [:div.column.is-8
         [:div.card>div.card-content>div.content
          [add-time-user-badge (:timestamp proposal) (:participant proposal)]
          (:text proposal)]]])
     [:div.columns
      [:div.column.is-3 (tr [:k/discussion] "discussion before proposal:")]
      [:div.column.is-8
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

  (let [iter-with-idx (map (fn [iter key] {:key key :iter iter}) (-> k :konsent :iterations) (range))]
    [:<>
     (for [i (reverse iter-with-idx)]
       ^{:key (:key i)}
       [one-iteration (:iter i) (inc (:key i)) k])]))



(defn debugging [k u]
  [:div
   [:div.is-divider.mt-6.mb-6 {:data-content "DEBUGGING"}]
   [:div.columns
    [:div.column.is-1 "DBG"]
    [:div.column.is-11
     ;[:pre (str "wait for = " (k-fsm/wait-for-other-users-to-be-ready k u))]
     [:pre (str "user = " u)]
     [:pre (str "next-actions-all-users     = " (with-out-str (cljs.pprint/pprint (k-fsm/next-actions-all-user k))))]
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
      #_[:div.column.is-3
         [:button.button.is-primary
          (if u
            {:on-click #(rf/dispatch [:konsent/de-activate])}
            not-logged-in)
          [:span.icon.is-large>i.fas.fa-1x.fa-arrow-alt-circle-left]
          [:div (tr [:k/btn-back-to-all] "back to all konsents")]]]

      [:div.column
       (if u
         [:div
          ;[short-name-and-problemstatement sn probs]
          ;[:div.is-divider.mt-6.mb-6 {:data-content "ask a question only to understand proposal"}]
          ;[ask]
          ;[:div.is-divider.mt-6.mb-6 {:data-content "free discussion to learn views, feelings, facts, opinions"}]
          (when (k-fsm/accepted? k)
            [:div
             [:div.is-divider.mt-6.mb-6 {:data-content (tr [:k/proposal-accepted] "Proposal ACCEPTED")}]
             [accepted k u]
             #_(when (k-fsm/user-is-proposer? k u)
                 [force-vote k u])])

          (when (and (k-fsm/voting-started? k) (k-fsm/user-voted? k u) (not (k-fsm/user-is-proposer? k u)) (not (k-fsm/accepted? k)))
            [:div
             [:div.is-divider.mt-6.mb-6 {:data-content (tr [:k/div-wainting-for-others-to-vote] "waiting for others to vote")}]
             [wait-for-everybody-voted k u]
             [my-vote (k-fsm/active-iteration k) u]
             #_(when (k-fsm/user-is-proposer? k u)
                 [force-vote k u])])

          (when (and (k-fsm/voting-started? k) (k-fsm/user-is-proposer? k u) (not (k-fsm/accepted? k)))
            [:div
             [:div.is-divider.mt-6.mb-6 {:data-content (tr [:k/force-end-of-voting] "force end of voting?")}]
             [force-vote k u]])

          (when (and (k-fsm/voting-started? k) (not (k-fsm/user-voted? k u)) (not (k-fsm/accepted? k)))
            [:div
             [:div.is-divider.mt-6.mb-6 {:data-content (tr [:k/voting-time] "voting time...")}]
             [vote-for-konsent k u]
             [wait-for-everybody-voted k u]])

          (when (and (k-fsm/wait-for-other-users-to-be-ready k u)
                     (k-fsm/user-is-proposer? k u)
                     (not (k-fsm/voting-started? k)))
            [:div
             [:div.is-divider.mt-6.mb-6 {:data-content (tr [:k/go-ahead?] "go ahead?")}]
             [wait-for-ready k u]
             [force-ready k u]])
          (when (and (k-fsm/wait-for-other-users-to-be-ready k u)
                     (not (k-fsm/user-is-proposer? k u))
                     (not (k-fsm/voting-started? k)))
            [:div
             [:div.is-divider.mt-6.mb-6 {:data-content (tr [:k/please-wait] "please wait")}]
             [wait-for-ready k u]
             [discuss-social]])
          (when (k-fsm/ask? k u)
            [:div
             [:div.is-divider.mt-6.mb-6 {:data-content "understand proposal"}]
             [ask k]])
          (when (k-fsm/answer? k u)
            [:div
             [:div.is-divider.mt-6.mb-6 {:data-content (tr [:k/answer-questions] "answer questions regarding your proposal")}]
             [answer k]])
          ;[discuss-social]])

          (when (k-fsm/discuss? k u)
            [:div
             [:div.is-divider.mt-6.mb-6 {:data-content (tr [:k/free-discussion] "free discussion to learn views, feelings, facts, opinions")}]
             [discuss]])
          ;[discuss-social]])

          (when (k-fsm/propose? k u)
            [:div
             [:div.is-divider.mt-6.mb-6 {:data-content (tr [:k/sep-create-proposal] "create proposal")}]
             [propose]])
          ;[propose-social]])
          ;[:div.is-divider.mt-6.mb-6 {:data-content "or start a konsent by suggesting"}]
          ;[vote-for-konsent]
          ;[:div.is-divider.mt-6.mb-6 {:data-content "start konsent"}]
          ;[start-konsent]
          ;[:div.is-divider.mt-6.mb-6 {:data-content "what happend before..."}]
          [history k]
          [:div.is-divider.mt-6.mb-6 {:data-content (tr [:k/started-by-with-participants] "started by with participants")}]
          [short-name-and-problemstatement sn probs]
          [participants ps]
          [created-by-when (-> k :konsent :owner) (-> k :konsent :timestamp)]]
         ;[debugging k u]]
         [konsent-example-list])]]]))







(defn my-konsents-page []
  [:section.section>div.container>div.content
   [:div.columns
    [:div.column.is-4 [konsent-list-page]]
    [:div.column.is-7
     (if @(rf/subscribe [:konsent/active])
       [konsent-active])]]])
;[konsent-list-page])]]])

(defn my-konsent-page [{{{:keys [id]} :path} :parameters}]
  ;(println "my-konsent-page data: " id)
  ;(println "going to dispatch :konsent/activate")
  ;(rf/dispatch {:ms 1000 :dispatch [:konsent/activate {:id id}]})
  [:section.section>div.container>div.content
   [:div "BEL data: " id]])

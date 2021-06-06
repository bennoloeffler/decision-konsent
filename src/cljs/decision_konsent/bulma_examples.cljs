(ns decision-konsent.bulma-examples
  (:require [decision-konsent.modal :as m]))


(defn example-horizontal-form []
  [:div
   [:div.field.is-horizontal
    [:div.field-label.is-normal
     [:label.label "From"]]
    [:div.field-body
     [:div.field
      [:p.control.is-expanded.has-icons-left
       [:input.input {:type "text" :placeholder "Name"}]
       [:span.icon.is-small.is-left
        [:i.fas.fa-user]]]]
     [:div.field
      [:p.control.is-expanded.has-icons-left.has-icons-right
       [:input.input.is-success {:type "email" :placeholder "Email" :value "alex@smith.com"}]
       [:span.icon.is-small.is-left
        [:i.fas.fa-envelope]]
       [:span.icon.is-small.is-right
        [:i.fas.fa-check]]]]]]
   [:div.field.is-horizontal
    [:div.field-label]
    [:div.field-body
     [:div.field.is-expanded
      [:div.field.has-addons
       [:p.control
        [:a.button.is-static "+44"]]
       [:p.control.is-expanded
        [:input.input {:type "tel" :placeholder "Your phone number"}]]]
      [:p.help "Do not enter the first zero"]]]]
   [:div.field.is-horizontal
    [:div.field-label.is-normal
     [:label.label "a veeeeeeeery long Department"]]
    [:div.field-body
     [:div.field.is-narrow
      [:div.control
       [:div.select.is-fullwidth
        [:select
         [:option "Business development"]
         [:option "Marketing"]
         [:option "Sales"]]]]]]]
   [:div.field.is-horizontal
    [:div.field-label
     [:label.label [:button.button.is-rounded.is-small "benno.loeffler@gmx.de"]]]
    [:div.field-body
     [:div.field.is-narrow
      [:div.control
       [:label.radio
        [:input {:type "radio" :name "member"}] "Yes"]
       [:label.radio
        [:input {:type "radio" :name "member"}] "No"]]]]]
   [:div.field.is-horizontal
    [:div.field-label.is-normal
     [:label.label "Subject"]]
    [:div.field-body
     [:div.field
      [:div.control
       [:input.input.is-danger {:type "text" :placeholder "e.g. Partnership opportunity"}]]
      [:p.help.is-danger "This field is required"]]]]
   [:div.field.is-horizontal
    [:div.field-label.is-normal
     [:label.label "just text"]]
    [:div.field-body
     [:div.field
      [:div.control
       [:input.input.is-light {:on-change (fn [_] nil) :value "Explain how we can help you"}]]]]]
   [:div.field.is-horizontal
    [:div.field-label.is-normal
     [:label.label "Question"]]
    [:div.field-body
     [:div.field
      [:div.control
       [:textarea.textarea {:placeholder "Explain how we can help you"}]]]]]
   [:div.field.is-horizontal
    [:div.field-label]
    [:div.field-body
     [:div.field
      [:div.control
       [:button.button.is-primary "Send message"]]]]]])

(defn example-box []
  [:div.box
   [:article.media
    [:div.media-left
     [:figure.image.is-64x64
      [:img {:src "https://bulma.io/images/placeholders/128x128.png" :alt "Image"}]]]
    [:div.media-content
     [:div.content
      [:p
       [:strong "John Smith"] [:small "@johnsmith"] [:small "31m"]
       [:br] "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean efficitur sit amet massa fringilla egestas. Nullam condimentum luctus turpis."]]
     [:nav.level.is-mobile
      [:div.level-left
       [:a.level-item {:aria-label "reply"}
        [:span.icon.is-small
         [:i.fas.fa-reply {:aria-hidden "true"}]]]
       [:a.level-item {:aria-label "retweet"}
        [:span.icon.is-small
         [:i.fas.fa-retweet {:aria-hidden "true"}]]]
       [:a.level-item {:aria-label "like"}
        [:span.icon.is-small
         [:i.fas.fa-heart {:aria-hidden "true"}]]]]]]]])

(defn participants []
  [:div.buttons.are-small
   [:button.button.is-rounded "benno.loeffler@gmx.de"]
   [:button.button.is-primary.is-outlined.is-light.is-rounded "Rounded"]
   [:button.button.is-link.is-rounded "Rounded"]
   [:button.button.is-link "Rounded"]
   [:button.button.is-success.is-rounded "Rounded"]
   [:button.button.is-danger.is-rounded "Rounded"]])

(defn example-badge []
  [:button.button [:span.badge {:title "Badge top right"} "8"] "Button"])


(defn example-tagsinput []
  ;(let [_ (.attach js/BulmaTagsInput)]
  [:div
   [:div.field
    [:label.label "Tags"]
    [:div.control
     [:select {:data-type "tags" :data-placeholder "Choose Tags"}
      [:option {:value "one" :selected "true"} "One"]
      [:option {:value "two"} "Two"]]]]
   [:div.field
    [:label.label "Tags"]
    [:div.control
     [:input.input {:type "text" :data-type "tags" :placeholder "Choose Tags" :value "One,Two"}]]]])

(defn example-collapsible []
  [:div#accordion_second
   [:article.message
    [:div.message-header
     [:p "Question 1" [:a {:href "#collapsible-message-accordion-second-1" :data-action "collapse"} "Collapse/Expand"]]]
    [:div#collapsible-message-accordion-second-1.message-body.is-collapsible {:data-parent "accordion_second" :data-allow-multiple "true"}
     [:div.message-body-content "Lorem ipsum dolor sit amet, consectetur adipiscing elit." [:strong "Pellentesque risus mi"] ", tempus quis
				placerat ut, porta nec nulla. Vestibulum rhoncus ac ex sit amet fringilla. Nullam gravida purus diam, et dictum"
      [:a "felis venenatis"] "efficitur. Aenean ac" [:em "eleifend lacus"] ", in mollis lectus. Donec sodales, arcu et
				sollicitudin porttitor, tortor urna tempor ligula, id porttitor mi magna a neque. Donec dui urna, vehicula et
				sem eget, facilisis sodales sem."]]]
   [:article.message
    [:div.message-header
     [:p "Question 2" [:a {:href "#collapsible-message-accordion-second-2" :data-action "collapse"} "Collapse/Expand"]]]
    [:div#collapsible-message-accordion-second-2.message-body.is-collapsible {:data-parent "accordion_second" :data-allow-multiple "true"}
     [:div.message-body-content "Lorem ipsum dolor sit amet, consectetur adipiscing elit." [:strong "Pellentesque risus mi"] ", tempus quis
				placerat ut, porta nec nulla. Vestibulum rhoncus ac ex sit amet fringilla. Nullam gravida purus diam, et dictum"
      [:a "felis venenatis"] "efficitur. Aenean ac" [:em "eleifend lacus"] ", in mollis lectus. Donec sodales, arcu et
				sollicitudin porttitor, tortor urna tempor ligula, id porttitor mi magna a neque. Donec dui urna, vehicula et
				sem eget, facilisis sodales sem."]]]
   [:article.message
    [:div.message-header
     [:p "Question 3" [:a {:href "#collapsible-message-accordion-second-3" :data-action "collapse"} "Collapse/Expand"]]]
    [:div#collapsible-message-accordion-second-3.message-body.is-collapsible {:data-parent "accordion_second" :data-allow-multiple "true"}
     [:div.message-body-content "Lorem ipsum dolor sit amet, consectetur adipiscing elit." [:strong "Pellentesque risus mi"] ", tempus quis
				placerat ut, porta nec nulla. Vestibulum rhoncus ac ex sit amet fringilla. Nullam gravida purus diam, et dictum"
      [:a "felis venenatis"] "efficitur. Aenean ac" [:em "eleifend lacus"] ", in mollis lectus. Donec sodales, arcu et
				sollicitudin porttitor, tortor urna tempor ligula, id porttitor mi magna a neque. Donec dui urna, vehicula et
				sem eget, facilisis sodales sem."]]]])


(defn example-icon-picker []
  [:input#iconPicker {:type "text" :data-action "iconPicker" :value "fa fa-icon fa-star"}] [:p.help "Click on icon to open the iconPicker."])


(def divider [:div.is-divider.mt-6.mb-6 {:data-content "OR"}])

(defn all-examples []
  [:div
   divider
   [example-horizontal-form]
   divider
   [m/modal-button
    :modal-id "modal"
    [participants]
    [:div
     [:button.button.is-primary.is-outlined "yes!"]
     [:button.button "caaaancel!"]]]
   [:div.is-divider.mt-6.mb-6 {:data-content "OR"}]
   [example-box]
   [:div.is-divider.mt-6.mb-6 {:data-content "OR"}]
   [example-badge]
   [:div.is-divider.mt-6.mb-6 {:data-content "OR"}]
   [example-tagsinput]
   [:div.is-divider {:data-content "OR"}]
   [example-collapsible]
   [:div.is-divider {:data-content "OR"}]
   [participants]
   [example-icon-picker]])
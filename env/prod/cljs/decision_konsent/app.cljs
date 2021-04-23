(ns decision-konsent.app
  (:require [decision-konsent.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)

(ns decision-konsent.client-time-test
  (:require [clojure.test :refer :all])
  (:require [decision-konsent.client-time :refer [show-time]]))

(deftest show-time-test
  (is (= "now" (show-time 1 1))))

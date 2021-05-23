(ns decision-konsent.db.core-test
  (:require
    [decision-konsent.db.core :refer [*db*] :as db]
    [java-time.pre-java8]
    [luminus-migrations.core :as migrations]
    [clojure.test :refer :all]
    [next.jdbc :as jdbc]
    [decision-konsent.config :refer [env]]
    [mount.core :as mount]
    [clojure.data.json :as json]
    [clojure.tools.logging :as log])
  (:import (java.util Date)))

(def nested [ {:name "Benno"
               :kids {:paul 19 :benno 22 :leo 14}}
              {:name "Sabine"
               :husband "Benno"
               :age 48}])
              ;(Date.)])

;(def json-txt (json/write-str nested))
;
(use-fixtures
  :once
  (fn [f]
    (mount/start
     #'decision-konsent.config/env
     #'decision-konsent.db.core/*db*)
    (migrations/migrate ["rollback"] (select-keys env [:database-url]))
    (migrations/migrate ["rollback"] (select-keys env [:database-url]))
    (migrations/migrate ["rollback"] (select-keys env [:database-url]))

    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-users
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (is (= 1 (db/create-user!
              t-conn
              {:email    "sam.smith@example.com"
               :password "pass"}
              {})))
    (is (= {:email    "sam.smith@example.com"
            :password "pass"}
           (db/get-user t-conn {:email "sam.smith@example.com"} {})))))


(deftest test-messages
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (is (= 1 (db/create-message!
              t-conn
              {:message      "first message"}
              {})))
    (is (= 1 (db/create-message!
              t-conn
              {:message      "second message"}
              {})))
    (is (= [ {:message      "first message"}
             {:message      "second message"}]
           (db/get-messages t-conn {})))))


(deftest test-konsent-json-write-read
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (is (= 1 (db/create-konsent!
              t-conn
              {:id         1
               :konsent    nested}
              {})))
    (is (= nested
           (:konsent (db/get-konsent t-conn {:id 1} {}))))))

(deftest test-konsent-no-id
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [id (db/create-konsent-no-id!
              t-conn
              {:konsent nested}
              {})
           _ (log/debug (str "ID: " id))]
      (is (int? (:id id)))
      (is (= nested
             (:konsent (db/get-konsent t-conn {:id (:id id)} {})))))))
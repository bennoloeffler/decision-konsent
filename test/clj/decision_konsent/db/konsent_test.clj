(ns decision-konsent.db.konsent-test
  (:require [clojure.test :refer :all])
  (:require [decision-konsent.db.core :as db])
  (:require [decision-konsent.db.konsent :as k])
  (:require
    [luminus-migrations.core :as migrations]
    [clojure.test :refer :all]
    [next.jdbc :as jdbc]
    [decision-konsent.config :refer [env]]
    [mount.core :as mount]
    [clojure.tools.logging :as log]))


(use-fixtures
  :once
  (fn [f]
    (mount/start
     #'decision-konsent.config/env
     #'decision-konsent.db.core/*db*)
    ;(migrations/migrate ["rollback"] (select-keys env [:database-url]))
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))


(deftest create-konsent!-test
  (jdbc/with-transaction [t-conn decision-konsent.db.core/*db* {:rollback-only true}]
    (let [data (k/create-konsent! t-conn
                {:owner "bel@something.de"
                 :problem-statement "problem Beschreibung üß"})]
;          _ (log/debug (str "DATA: " data))]
      (is (int? (-> data :id)))
      (is (int? (-> data :konsent :timestamp)))
      (is (= "bel@something.de" (-> data :konsent :owner)))
      (is (= "problem Beschreibung üß" (-> data :konsent :problem-statement))))))

(deftest save-konsent!-test
  (jdbc/with-transaction [t-conn decision-konsent.db.core/*db* {:rollback-only true}]
   (let [initial-data (k/create-konsent! t-conn
                       {:owner "bel2@something.de"
                        :problem-statement "problem Beschreibung üß"})
         id           (-> initial-data :id)
;         _            (log/debug (str "INITIAL: " initial-data))
         data         (assoc-in  initial-data [:konsent :owner] "bel3")
;          _            (log/debug (str "DATA: " data))
         num          (k/save-konsent! t-conn data)
         changed-data (db/get-konsent t-conn {:id id})]
;         _            (log/debug (str "CHANGED-DATA: " changed-data))]
     (is (= id (-> changed-data :id)))
     (is (int? (-> changed-data :konsent :timestamp))) ; different!
     (is (= "bel3" (-> changed-data :konsent :owner)))
     (is (= "problem Beschreibung üß" (-> changed-data :konsent :problem-statement))))))


(deftest get-konsents-test ; DONT KNOW WHY IT FAILS WITH t-conn
  ;(jdbc/with-transaction [t-conn decision-konsent.db.core/*db* {:rollback-only true}]
   (let [_            (k/create-konsent! #_t-conn
                       {:owner "hah@something.de"
                        :problem-statement "px"})
         read-data    (k/get-konsents #_t-conn)
         _            (log/debug (str "READ-DATA: " read-data))
         num-rows     (count read-data)]
;          _            (log/debug (str "konsent ROWS: " num-rows))])))
     (is (= 1 num-rows)))) ;dont understand that problem - should be one

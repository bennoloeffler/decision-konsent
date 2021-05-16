(ns decision-konsent.db.konsent
  (:require [decision-konsent.db.core :as db]))

(defn now []
  (long (/ (System/currentTimeMillis) 1000)))

(defn create-konsent!
  "create an empty konsent based on
   {:owner \"XXX@xy.z\" :problem-description \"pd\" }
   returns an id-konsent-map {:id INT :konsent {DATA}}"
  ([owner-problem-statement-map]
   (create-konsent! nil owner-problem-statement-map))
  ([t-conn { :keys [owner problem-statement]}]
   (let [konsent {:owner owner
                  :problem-statement problem-statement
                  :timestamp (now)}
         id (:id (db/create-konsent-no-id! t-conn {:konsent konsent}))]
     {:id id :konsent konsent})))

(defn save-konsent!
  "saves are done by data formed like {:id 5 :konsent {:something}}"
  ([id-konsent-map] save-konsent! nil id-konsent-map)
  ([t-conn id-konsent-map]
   (if (db/update-konsent! t-conn id-konsent-map)
       id-konsent-map
       nil)))
;
(defn delete-konsent!
  "deleted by :id {:id 5 :konsent {:something}}"
  [id-konsent-map]
  (db/delete-konsent! id-konsent-map))

(defn get-konsents
  "get all"
  ([] get-konsents nil)
  ([t-conn]
   (db/get-konsents t-conn)))
;
#_(defn get-konsents-for
    [member]
    (db/get-consent-for member))
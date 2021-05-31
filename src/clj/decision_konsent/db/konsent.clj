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
  ([t-conn konsent]
   (let [konsent       (into konsent {:timestamp (now)})
         id            (:id (if t-conn
                              (db/create-konsent-no-id! t-conn {:konsent konsent})
                              (db/create-konsent-no-id! {:konsent konsent})))
         saved-konsent {:id id :konsent konsent}]
     (println "saved konsent: " saved-konsent)
     saved-konsent)))


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
  ([] (db/get-konsents))
  ([t-conn]
   (db/get-konsents t-conn)))


(defn get-konsents-for-user [email] ;TODO do this in database WITHOUT loading all konsents
  (let [all       (db/get-konsents)
        as-owner (filter #(= (:email email) (-> % :konsent :owner)) all)
        as-part  (filter #(some #{(:email email)} (-> % :konsent :participants)) all)]
     ;combine
     ;(println "get my konsents: " email)
     ;(println "all: " all)
     ;(println "as owner: " as-owner)
     ;(println "as parti: " as-part)
     (distinct (into as-owner as-part))))

#_(defn get-konsents-for-user [email]
   (let [as-owner (db/get-konsents-for-user-as-owner email)
         as-participant (db/get-konsents-for-user-as-participant email)]
        ;combine
        (println "get my konsents: " email)
        (println "as owner: " as-owner)
        (println "as parti: " as-participant)
        (into as-owner as-participant)))
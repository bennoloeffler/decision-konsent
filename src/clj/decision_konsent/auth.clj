(ns decision-konsent.auth
 (:require [decision-konsent.db.core :as db]
           [buddy.hashers :as hashers]
           [next.jdbc :as jdbc]))


(defn create-user! [email password]
  (jdbc/with-transaction [t-conn db/*db*]
    (if-not (empty? (db/get-user t-conn {:email email}))
            (throw (ex-info "User exists"
                            {:decision-konsent/error-id ::duplicate-user
                             :error "User exists"}))
            (db/create-user! t-conn
                             {:email email
                              :password (hashers/derive password)}))))


(defn authenticate-user [email password]
  (let [{hashed :password :as user} (db/get-user {:email email})]
    (when (hashers/check password hashed)
      (dissoc user :password))))
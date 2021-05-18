(ns decision-konsent.db.names)


(def first-names [ "Benno"
                   "Franz"
                   "Fritz"
                   "Otto"
                   "Joe"
                   "Jim"
                   "Tim"
                   "Jan"
                   "Leo"
                   "Tom"
                   "Max"])


(def last-names ["Weber"
                 "Müller"
                 "Kunz"
                 "Hinz"
                 "Schmitt"
                 "Wolf"
                 "Klein"
                 "Maier"
                 "Fischer"
                 "Wagner"
                 "Schulz"
                 "Bauer"
                 "Richter"])


(def domain-fragments ["super"
                       "max"
                       "ultra"
                       "extreme"
                       "unreal"
                       "over"
                       "special"
                       "turbo"
                       "hot"
                       "big"
                       "more"
                       "better"
                       "bigger"
                       "hotter"])


(def password-fragments ["iU6&"
                         "eW1="
                         "uK4%"
                         "0=uZ"
                         "8(gH"
                         "3$pG"
                         "p=U/"
                         "j4D("
                         "D&j7"])


(defn get-rand-double-name [names]
  (assert (> (count names) 1))
  (let [names-range (count names)
        n1 (nth names (rand-int names-range))
        n2 (nth names (rand-int names-range))]
    (if (= n1 n2)
        (get-rand-double-name names)
        (str n1 "-" n2))))


(defn get-email [first last]
 (let [dom (get-rand-double-name domain-fragments)]
   (str first "." last "@" dom ".com")))


(defn create-rand-user []
  (let [first (get-rand-double-name first-names)
        last  (get-rand-double-name last-names)
        email  (get-email first last)
        password  (get-rand-double-name password-fragments)]
    {:id (str (rand-int 100000000))
     :first_name first
     :last_name last
     :email email
     :password password}))


(comment
  (get-rand-double-name last-names)
  (get-first-name)
  (get-last-name)
  (create-rand-user))
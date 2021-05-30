(ns decision-konsent.utils)

(def prevent-reload {:on-submit (fn [e] (do (.preventDefault e)
                                            (identity false)))})

(defn emails-split [emails-text]
  (as-> emails-text $
      (clojure.string/split $ #",| |;|\n")
      (remove clojure.string/blank? $)
      (vec $)))
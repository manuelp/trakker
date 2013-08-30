(ns trakker.search)

(defn- contains-pattern [substring]
  (re-pattern (str ".*" substring ".*")))

(defn desc?
  "Predicate that checks if the given entry has a substring s in his description."
  [entry s]
  (let [pattern (contains-pattern s)
        description (:desc entry)]
    (not (nil? (re-matches pattern description)))))


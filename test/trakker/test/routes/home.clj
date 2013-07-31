(ns trakker.test.routes.home
  (:use clojure.test
        trakker.routes.home))

(deftest tabs-link-generation
  (testing "Should build the tabs map according to the current active one."
    (let [tabs-map {:a {:title "a"
                        :url "/a"}
                    :b {:title "b"
                        :url "/b"}}]
      (is (= [{:title "a"
               :url "/a"
               :active true}
              {:title "b"
               :url "/b"
               :active false}]
             (gen-tabs tabs-map :a)))
      (is (= [{:title "a"
               :url "/a"
               :active false}
              {:title "b"
               :url "/b"
               :active true}]
             (gen-tabs tabs-map :b))))))



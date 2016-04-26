(ns org.timmc.pellucida.db-test
  (:use clojure.test)
  (:require [org.timmc.pellucida.db :as db]))

(deftest std-query-opts
  (testing "Doesn't casefold identifiers"
    (is (= ((:identifiers db/std-query-opts) "FooBAR")
           "FooBAR"))))

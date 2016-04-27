(ns org.timmc.pellucida.util-test
  (:use clojure.test)
  (:require [org.timmc.pellucida.util :as u]))

(deftest uri-codecs
  (is (= (u/enc-pathc  "/?#% +") "%2F%3F%23%25%20%2B"))
  (is (= (u/enc-queryc "/?#% +") "%2F%3F%23%25%20%2B"))
  (is (= (u/dec-pathc  "%2F%3F%23%25%20%2B+") "/?#% ++"))
  (is (= (u/dec-queryc "%2F%3F%23%25%20%2B+") "/?#% + ")))

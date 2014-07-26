(ns org.timmc.pellucida.util-test
  (:use clojure.test
        org.timmc.pellucida.util))

(deftest uri-codecs
  (is (= (enc-pathc  "/?#% +") "%2F%3F%23%25%20%2B"))
  (is (= (enc-queryc "/?#% +") "%2F%3F%23%25%20%2B"))
  (is (= (dec-pathc  "%2F%3F%23%25%20%2B+") "/?#% ++"))
  (is (= (dec-queryc "%2F%3F%23%25%20%2B+") "/?#% + ")))

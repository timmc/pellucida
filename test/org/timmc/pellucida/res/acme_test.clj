(ns org.timmc.pellucida.res.acme-test
  (:use clojure.test)
  (:require [org.timmc.pellucida.res.acme :as a]
            [org.timmc.pellucida.settings :as cnf]))

(deftest is-token-shaped?
  (testing "Example from spec"
    (is (= (a/is-token-shaped? "evaGxfADs6pSRb2LAv9IZf17Dt3juxGJ-PCt92wr-oA")
           true)))
  (testing "Base-64, URL variant"
    (is (= (a/is-token-shaped? "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_=")
           true)))
  (testing "Empty"
    (is (= (a/is-token-shaped? "") false)))
  (testing "Anchored regex"
    (is (= (a/is-token-shaped? "...lqiegqweigwejlie...") false)))
  (testing "No slashes or dots or other tom-foolery"
    (is (= (a/is-token-shaped? "magic-token") true))
    (is (= (a/is-token-shaped? "magic.token") false))
    (is (= (a/is-token-shaped? "magic/token") false))
    (is (= (a/is-token-shaped? "magic\u0000token") false))))

(deftest token-response
  (testing "Checks token sanity itself"
    (is (nil? (a/token-response "bogus/value"))))
  (testing "Happy path"
    (with-redefs [cnf/config (atom {:acme-challenge-dir "/base"})]
      (let [resp (a/token-response "not-bogus")]
        (is (= (:status resp) 200))
        (is (= (:headers resp) {"Content-Type" "text/plain"}))
        (is (= (.getPath ^java.io.File (:body resp))
               "/base/not-bogus"))))))

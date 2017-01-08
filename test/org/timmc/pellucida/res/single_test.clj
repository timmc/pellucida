(ns org.timmc.pellucida.res.single-test
  (:use clojure.test)
  (:require [org.timmc.pellucida.res.single :as s]))

(deftest text-to-html-nodes
  (let [br {:tag :br, :attrs {}, :content []}]
    (testing "Base cases"
      (is (= (s/text-to-html-nodes nil) nil))
      (is (= (s/text-to-html-nodes "") [""])))
    (testing "Double linebreaks and single linebreaks treated same way"
      (is (= (s/text-to-html-nodes "paragraph\n\nanother")
             ["paragraph" br br "another"]))
      (is (= (s/text-to-html-nodes "- simple\n- bulleted\n- list")
             ["- simple" br "- bulleted" br "- list"])))
    (testing "Leading and trailing newlines preserved"
      (is (= (s/text-to-html-nodes "\n\nstuff\n\n")
             [br br "stuff" br br])))))

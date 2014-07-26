(ns org.timmc.pellucida.pager-test
  (:use clojure.test
        org.timmc.pellucida.pager))

(deftest paging
  (testing "normal usage"
    (are [f t out] (= (paging-segment f t {:neighborhood 2 :min-elide 2}) out)
         10 0 []
         3 3 []
         3 4 []
         0 2 [1]
         0 3 [1 2]
         0 4 [1 2 3]
         0 5 [1 2 3 4]
         0 6 [1 2 ,3, 4 5]
         0 7 [1 2 nil 5 6]
         0 10 [1 2 nil 8 9])
    (are [f c l out] (= (anchorhood f c l {:neighborhood 2 :min-elide 2}) out)
         0 0 0 [0]
         0 5 5 [0 1 2 3 4 5]
         0 6 6 [0 1 2 ,3, 4 5 6]
         0 0 7 [0 1 2 nil 5 6 7]
         0 7 10 [0 1 2 nil 5 6 7 8 9 10]))
  (testing "min neighborhood"
    (are [f t out] (= (paging-segment f t {:neighborhood 0 :min-elide 2}) out)
         0 1 []
         0 2 [1]
         0 3 [nil])
    (are [f c l out] (= (anchorhood f c l {:neighborhood 0 :min-elide 2}) out)
         0 2 4 [0 ,1, 2 ,3, 4]
         0 3 4 [0 nil 3 4]))
  (testing "min min-elide"
    (are [f t out] (= (paging-segment f t {:neighborhood 2 :min-elide 1}) out)
         0 1 []
         0 5 [1 2 3 4]
         0 6 [1 2 nil 4 5])
    (are [f c l out] (= (anchorhood f c l {:neighborhood 2 :min-elide 1}) out)
         0 6 12 [0 1 2 nil 4 5 6 7 8 nil 10 11 12])))

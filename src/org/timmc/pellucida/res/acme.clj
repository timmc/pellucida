(ns org.timmc.pellucida.res.acme
  "Pass through ACME requests to filesystem. (ACME: Automated
Certificate Management Environment)"
  (:require
   [compojure.core :refer [defroutes GET]]
   [org.timmc.pellucida.settings :as cnf]))

(defn is-token-shaped?
  "Does this string look like an ACME http-01 token? In particular,
does it *not* look like a path traversal attack?"
  [^String s]
  (boolean (re-matches #"[a-zA-Z0-9\-_=]+" s)))

(defn token-response
  "Answer with the contents of the ACME challenge file."
  [magic-token]
  ;; The ACME http-01 token value is used as the filename as well
  ;; as the value of a field within the JSON file.
  (when (is-token-shaped? magic-token)
    (let [resp-path (str (:acme-challenge-dir @cnf/config) "/" magic-token)]
      {:status 200
       :headers {"Content-Type" "text/plain"} ;; Spec: This or none
       :body (java.io.File. resp-path)})))

(defroutes acme-routes
  (GET ["/.well-known/acme-challenge/:magic-token"] [magic-token :as r]
       (when (:acme-challenge-dir @cnf/config)
         (token-response magic-token))))

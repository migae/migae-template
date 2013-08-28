(ns {{name}}.filter
  (:import (javax.servlet Filter FilterChain FilterConfig
                          ServletRequest ServletResponse))
  (:gen-class :implements [javax.servlet.Filter]))

(defn -init [^Filter this ^FilterConfig cfg])

(defn -doFilter
  [^Filter this
   ^ServletRequest rqst
   ^ServletResponse resp
   ^FilterChain chain]
  (do
    (require '{{name}}.request-impl '{{name}}.user-impl :reload);; :verbose)
    (.doFilter chain rqst resp)))

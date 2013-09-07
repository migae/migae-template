(ns {{name}}.reload-filter
  (:import (javax.servlet Filter FilterChain FilterConfig
                          ServletRequest ServletResponse))
  (:require [clojure.tools.logging :as log :only [debug info]])
  (:gen-class :implements [javax.servlet.Filter]))

(defn -init [^Filter this ^FilterConfig cfg])

(defn -doFilter
  [^Filter this
   ^ServletRequest rqst
   ^ServletResponse resp
   ^FilterChain chain]
  (do
    (log/info "reloading...")
    (require
     {{#servlets}}
     '{{appname}}.{{servlet}}-impl
     {{/servlets}}
     ;; :verbose
     :reload)
    (.doFilter chain rqst resp)))

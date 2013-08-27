(ns {{project}}.core
  (:require [migae.core :as ae]))

(defn {{project}}-app-handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello, world!"})

(ae/def-appengine-app {{project}}-app #'{{project}}-app-handler)

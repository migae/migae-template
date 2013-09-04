(ns {{ns}}-impl
  (:use compojure.core
        [ring.middleware.params :only [wrap-params]]
        [ring.middleware.file-info :only [wrap-file-info]])
  (:require [compojure.route :as route]))

(defroutes {{name}}-routes
  {{#services}}
  (GET "{{url-pattern}}" []
    (str (format "<h1>Ohayo from {{ns}} servlet path {{url-pattern}}!</h1>")
         "\n\n<a href='/'>home</a>"))
  {{/services}}

  (route/not-found "<h1>Page not found</h1>"))

(def {{name}}-handler
  (-> #'{{name}}-routes
      wrap-params
      wrap-file-info
      ))


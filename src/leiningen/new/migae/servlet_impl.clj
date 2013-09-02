(ns {{name}}.{{class}}-impl
  (:use compojure.core
        [ring.middleware.params :only [wrap-params]]
        [ring.middleware.file-info :only [wrap-file-info]])
  (:require [compojure.route :as route]))

(defroutes {{class}}-routes
  (GET "/{{class}}/hello/:you" [you]
    (str (format "<h1>Ohayo %s from myapp.{{class}} servlet!</h1>" you)
         "\n\n<a href='/'>home</a>"))
  (GET "/{{class}}/goodbye/:you" [you]
    (str (format "<h1>Sayonara %s from myapp.{{class}} servlet!</h1>" you)
         "\n\n<a href='/'>home</a>"))
  (route/not-found "<h1>Page not found</h1>"))

(def {{class}}-handler
  (-> #'{{class}}-routes
      wrap-params
      wrap-file-info
      ))


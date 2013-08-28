(ns {{name}}.{{class}}-impl
  (:use compojure.core
        [ring.middleware.params :only [wrap-params]]
        [ring.middleware.file-info :only [wrap-file-info]])
  (:require [compojure.route :as route]))

(defroutes {{class}}-routes
  (GET "/{{class}}/hello" [] "<h1>Hello World from {{name}}.{{class}} servlet!</h1>")
  (GET "/{{class}}/goodbye" [] "<h1>Goodbye World! from {{name}}.{{class}} servlet!</h1>")
  (route/not-found "<h1>Page not found</h1>"))

;; (defroutes theRouter
;; (defroutes {{class}}-routes
;; {{#services}}
;;   (GET "{{route}}" [{{#arg}}{{var}}{{/arg}}]
;;     {:status 200
;;      :headers {"Content-Type" "text/html"}
;;      :body (format "This is the <i>{{action}} {{svcname}}</i> service of the <i><b>{{name}}.{{class}}</b></i> servlet. {{#arg}}  Now serving <i>%s</i>." {{var}}{{/arg}}{{^arg}}"{{/arg}})})

;; {{/services}}

;;  (route/files "/" {:root "/public/"})

  ;; (route/not-found "Sorry, {{name}}.{{class}} page not found\n"))

(def {{class}}-handler
  (-> #'{{class}}-routes
      wrap-params
      wrap-file-info
      ))


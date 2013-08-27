(ns {{name}}.{{class}}
    (:gen-class :extends javax.servlet.http.HttpServlet)
    (:use [ring.util.servlet :only [defservice]]
          [ring.middleware.params :only [wrap-params]]
          [ring.middleware.file-info :only [wrap-file-info]]
;          [ring.handler.dump :only [handle-dump]]
          [compojure.core])
    (:require [ring.handler.dump]
              [compojure.route :as route]
              [appengine-magic.kernel :as ae]
;              [appengine-magic.services.user :as aeu]
              [clojure.pprint :as pp]
              [clojure.tools.logging :as log :only [debug info]]))

(defroutes {{class}}-routes
{{#services}}
  (GET "{{route}}" [{{#arg}}{{var}}{{/arg}}]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (format "This is the <i>{{action}} {{svcname}}</i> service of the <i><b>{{name}}.{{class}}</b></i> servlet. {{#arg}}  Now serving <i>%s</i>." {{var}}{{/arg}}{{^arg}}"{{/arg}})})

{{/services}}

;;  (route/files "/" {:root "/public/"})

  (route/not-found "Sorry, {{name}}.{{class}} page not found\n"))

(def {{class}}-handler
  (-> #'{{class}}-routes
      wrap-params
      wrap-file-info
      ;; handle-dump
      ))

(println "prepping {{name}}-{{class}} servlet")
(defservice {{class}}-handler)

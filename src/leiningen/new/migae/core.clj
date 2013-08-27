(ns {{appname}}.core
    (:gen-class :extends javax.servlet.http.HttpServlet)
    (:use [gae.servlet :only [make-servlet-service-method]]
          [ring.middleware.params :only [wrap-params]]
          [ring.middleware.file-info :only [wrap-file-info]]
          [ring.handler.dump :only [handle-dump]]
          [compojure.core])
    (:require [ring.handler.dump]
              [compojure.route :as route]
              [migae.kernel :as ae]
              [migae.service.user :as aeu]
              [clojure.pprint :as pp]
              [clojure.tools.logging :as log :only [debug info]]))

(defroutes {{appname}}-routes
  (GET "/dump/request" rqst
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body "Why, hello there, world!"})
  ;; :body (with-out-str (pp/pprint rqst))})

;;  (route/files "/" {:root "/public/"})

  (route/not-found "Sorry, dump page not found\n"))

(def {{appname}}-handler
  (-> #'{{appname}}-routes
      wrap-params
      wrap-file-info
      ;; handle-dump
      ))


(ae/def-appengine-app {{appname}}-app #'{{appname}}-handler)

(defn -service [this request response]
  ((make-servlet-service-method {{appname}}-app) this request response))

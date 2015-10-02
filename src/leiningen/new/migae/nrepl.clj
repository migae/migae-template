(ns myapp.nrepl
    (:require [compojure.core :refer :all]
              [compojure.route :as route]
              [ring.handler.dump :refer :all] ; ring-devel
              [ring.middleware.params :refer [wrap-params]] ; ring-core
              [ring.middleware.keyword-params :refer [wrap-keyword-params]] ; ring-core
              [ring.middleware.nested-params :refer [wrap-nested-params]] ; ring-core
              [ring.middleware.defaults :refer :all] ; ring-defaults
              [ring.util.response :as rsp]
              [ring.util.servlet :as ring]
              [clojure.pprint :as pp]
              [clojure.tools.logging :as log :refer [debug info]]
              [cemerick.drawbridge :as repl]))


(def nrepl-handler
  (-> (repl/ring-handler)
      (wrap-keyword-params)
      (wrap-nested-params)
      (wrap-params)))
      ;; (session/wrap-session)))

(defroutes nrepl-routes
  (context "/nrepl" []
           (ANY "*" req (nrepl-handler req))
           (route/not-found "Sorry, dump page not found\n")))

(ring/defservice
  (-> (routes
       nrepl-routes)
      ))

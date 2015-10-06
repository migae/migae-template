(ns {{ns.sym}} .handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.handler.dump :refer [handle-dump]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as r]
            [ringx.util.http-response :as rx :refer :all]
            [iron-list.hiccup [starter-kit :as sk]]
            [iron-list.hiccup [iron-list :as il]]))

(defroutes app-routes
  (GET "/hello" [] "Hello World")

  (GET "/hiccup" []
    (-> (ok (str (il/main-page)))
      (r/content-type "text/html")))

  (GET "/starter" []
    (-> (ok (str (sk/main-page)))
      (r/content-type "text/html")))

  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

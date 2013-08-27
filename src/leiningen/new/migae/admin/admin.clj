(ns lex.admin
    (:gen-class :extends javax.servlet.http.HttpServlet)
    (:import [org.apache.commons.fileupload.util Streams])
    (:use [ring.util.servlet :only [defservice]]
          [ring.util.response :as resp]
          [ring.middleware.params :only [wrap-params]]
          [ring.middleware.file-info :only [wrap-file-info]]
          [hiccup.def :only [defhtml defelem]]
          [hiccup.page :only [include-css include-js]]
;          [ring.handler.dump :only [handle-dump]]
          [compojure.core])
    (:require [ring.handler.dump]
              [compojure.route :as route]
              (ring.middleware [multipart-params :as mp])
              [appengine-magic.kernel :as ae]
              [appengine-magic.service.user :as aeu]
              [clojure.pprint :as pp]
              [clojure.tools.logging :as log :only [debug info]]))

(def jslibs  (seq ["/js/normalizeconsole.min.js"
                   "http://code.jquery.com/jquery-1.9.1.js"
                   "/js/jquery.openid.js"]))
                   ;; "http://code.jquery.com/ui/1.10.2/jquery-ui.js"

(def csslibs
  (seq ["http://code.jquery.com/ui/1.10.2/themes/smoothness/jquery-ui.css"
        "/css/login.css"]))

(defhtml upload []
  [:head
   [:title "Login"]
   (apply include-js jslibs)
   (apply include-css csslibs)
   ]
  [:body
   [:form {:action "/u", :method "post", :enctype "multipart/form-data"}
    ;; <form action="/upload" method="post" enctype="multipart/form-data">
    [:input {:type "hidden", :name "fileOwner"}]
    ;; <input type="hidden" name="fileOwner" />
    ;;   <div>FileId:　　<input type="text" name="fileId" size="50" /></div>
    [:div
     [:input {:type "file", :name "upfile", :size "50"}]
     "FileObject: "]
    ;; <div>FileObject: <input type="file" name="upfile" size="50" /></div>
    [:div [:input {:type "submit", :name "submit", :value "Upload"}]]
    ;; <div><input type="submit" name="submit" value="Upload" /></div>
    ]
   ;; </form>
   ])

;; TODO:  auth: user must be admin
(defroutes admin-routes
  (GET "/admin" rqst
    (if (not (aeu/user-logged-in?))
      ;; (str "not logged in\nlogin dest: "
      ;;      (aeu/login-url :destination "/dump/user"))
      (resp/redirect "/_ah/login_required?continue=/admin")
      (let [user (str (aeu/current-user))]
        (upload))))

     ;;    {:status 200
     ;;     :headers {"Content-Type" "text/html"}
     ;; :body (format "This is the <i>GET admin</i> service of the <i><b>lexrest.admin</b></i> servlet.   Now serving <i>%s</i>." user)})))

  (mp/wrap-multipart-params
   (POST "/u" rqst
     ;; TODO:  auth:  user must be admin (or on hardcoded list?)
     ;; this will be "called" /after/ the :store fn passed as arg below:
     {:status 200
s     :headers {"Content-Type" "text/html"}
     :body ((rqst :multipart-params) "upfile")})
   ;; arg to wrap-multipart-params handles payload:
   {:store (fn [{:keys [filename content-type stream]}]
             (do ;;(log/info (format "filename: %s\ncontent-type: %s\n"
                   ;;                filename, content-type))
               ;;TODO: parse data, update datastore
               (Streams/asString stream)))})

  (route/not-found "Sorry, lexrest.admin page not found\n"))

(def admin-handler
  (-> #'admin-routes
      wrap-params
      wrap-file-info
      ;; handle-dump
      ))

(println "prepping lexrest-admin servlet")
(defservice admin-handler)

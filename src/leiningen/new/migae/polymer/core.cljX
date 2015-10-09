(ns {{ns.sym}}
  (:require
{{#clojure}}
	[clojure.pprint :as pp]
	[clojure.string :as string]
	[clojure.tools.logging :as log :refer [debug info]]
{{/clojure}}
{{#compojure}}
	[compojure.coercions :as coerc :refer [as-int as-uuid]]
	[compojure.core :refer :all]
	[compojure.handler :as handler :refer [api site]]
	[compojure.route :as route :refer [files resources]]
{{/compojure}}
{{#ring}}
	;; TODO: split out ring.middleware, ring.util, etc.???
	 [ring.handler.dump :refer [handle-dump]] ; ring-devel
	 [ring.middleware.keyword-params :refer [wrap-keyword-params]] ; ring-core
	 [ring.middleware.params :refer [wrap-params]] ; ring-core
         [ring.middleware.resource :refer [wrap-resource]]
	 ;; [ring.middleware.nested-params :refer [wrap-nested-params]] ; ring-core
	 [ring.middleware.defaults :refer :all] ; ring-defaults
	 [ring.util.response :as r]
{{/ring}}
{{#ringx}}
	 [ringx.util.http-response :refer :all]
{{/ringx}}
{{#hiccup}}
	 [hiccup.core :refer [h html]]
	 [hiccup.def :refer [defelem defhtml wrap-attrs]]
	 [hiccup.element :refer [image javascript-tag link-to mail-to ordered-list unordered-list]]
	 [hiccup.form :refer [*group* check-box drop-down email-field
	 	      	      file-upload form-to hidden-field label
			      password-field radio-button reset-button
			      select-options submit-button text-area text-field
			      with-group]]
	 [hiccup.middleware :refer [wrap-base-url]]
	 [hiccup.page :refer [doctype html4 html5
	 	      	      include-css include-js
			      xhtml xhtml-tag xml-declaration]]
	 [hiccup.util :refer [*base-url* *encoding* *html-mode*
	 	      	      as-str escape-html ToString ToURI
			      url URLEncode with-base-url with-encoding]]
{{/hiccup}}
{{#polymer}}
         [polymeraj.hiccup :as poly :refer :all]
{{/polymer}}
    ))

(defroutes {{project}}-routes
  (GET "/" []
    (println "myapp-routes")
    (co-ns foo.bar.myapp
      "polymer demo (docstring)"
      (:require [polymer.paper :as paper :refer [button material]])
      [:h1 "Hello World"]
      ;; (paper/material "I am a polymer paper-material component!")
      ;; (paper/button {:raised true} "Click me - I am a raised polymer button component!")
      [:paper-material "I am a polymer paper-material component!"]
      [:paper-button {:raised true} "Click me - I am a raised polymer button component!"]
      ))

  (route/not-found "Not Found"))

(def app
  (-> {{project}}-routes
    (wrap-resource "/")
    (wrap-defaults site-defaults)))

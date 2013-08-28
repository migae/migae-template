(ns leiningen.new.migae
  (:require  [clojure.java.io :as io]
             [clojure.string :only [join]])
  (:use
        [leiningen.new.templates :only [*dir* project-name
                                        renderer multi-segment
                                        sanitize-ns name-to-path
                                        year ->files]]
        [leiningen.core.main :only [abort]]))

;; main template entry point
(defn migae
  "A Leiningen template for a new migae project"
  [projname & args]
  ;; projname syntax:  <clj-app>:<gae-app-id>
  (do
    (if (nil? args)
      (do (abort "missing sdk path!  Syntax:  lein new migae myapp:gae-app-id path/to/sdk") )
      (println args))
    (let
        [[appname appid] (.split  projname ":")
         sdk (first args)
         render (renderer "migae")
         main-ns (multi-segment (sanitize-ns appname))
         data {:name appname ;; ":name" key required by leiningen
               :projdir (clojure.string/join "/"
                              [(System/getProperty "leiningen.original.pwd")
                                  appname])
               :appname appname
               :version "0.1.0-SNAPSHOT"
               :gae-app-id appid
               :gae-app-version "0-1-0"
               :raw-name appname
               :servlets [{:name appname, :class "request",
                           :services [{:svcname "request", :action "GET"
                                       :url-pattern "/request/*"
                                       :route "/request/:rqst"
                                       :arg {:var "rqst"}}]}
                          {:name appname, :class "user",
                           :services [{:svcname "user", :action "GET"
                                       :url-pattern "/user/*"
                                       :route "/user/:arg"
                                       :arg {:var "arg"}}
                                      {:svcname "login", :action "GET"
                                       :url-pattern "/_ah/login_required"
                                       :route "/_ah/login_required"}]}]
               :display-name (project-name appname)
               :project (project-name appname)
               :aots [{:aot (str appname ".request")}
                      {:aot (str appname ".user")}]
               :namespace (str appname ".request")
               :projroot (name-to-path appname) ;; foo-bar -> foo_bar
               :welcome "index.html"
               :sdk sdk
               :war "war"
               ;; we only install to src dirs, leave config to migae plugin
               :statics_src "src/main/public"
               :resources_src "src/main/resource"
               :year (year)
               :proj-url "http://example.com/FIXME"
               :nested-dirs (name-to-path main-ns) ;; foo-bar.core -> foo_bar/core
               :threads true
               :sessions true
               :java-logging "logging.properties"
               :log4j-logging "log4j.properties"}]
      (println "Generating an migae project called " appname ", app id " appid ", using the 'migae' template.")

;      (println "Generating servlet skeletons")
      (doseq [servlet (:servlets data)]
        (do ;(println servlet)
            (binding [*dir* (.getPath (io/file
                                       (System/getProperty
                                        "leiningen.original.pwd")
                                       (:name servlet)))]
              (->files servlet
                       ["src/{{name}}/{{class}}.clj"
                        (render "servlet.clj" servlet)]))))

;      (println "Generating other stuff")
      (binding [*dir* (.getPath (io/file (System/getProperty "leiningen.original.pwd") (:name data)))]
        (->files data
                 ;; to file  		from template
                 ["project.clj" (render "project.clj" data)]
                 ["README.md" (render "README.md" data)]
                 ["doc/intro.md" (render "intro.md" data)]
                 [".gitignore" (render "gitignore" data)]

                 [".dir-locals.el" (render "dir-locals.el" data)]

                 ;; ["src/main/java/com/google/apphosting/utils/security/SecurityManagerInstaller.java" (render "SecurityManagerInstaller.java" data)]

                 ;; NB: treatment of '-', '_', '/', and '.'
                 ;; "lein new migae foo-bar" yields:
                 ;; src:  ns foo-bar.core in  src/foo_bar/core.clj
                 ;; ["src/{{nested-dirs}}.clj" (render "core.clj" data)]

                 ;; ;; test: foo-bar.core-test -> foo_bar/core_test.clj
                 ["test/{{nested-dirs}}_test.clj" (render "core_test.clj" data)]

                 ["src/{{name}}/filter.clj"
                  (render "filter.clj" data)]


                 ;; app engine config files
                 ;; copy template w/o macro processing ('data' arg)
                 ;; install in hidden dir?  or etc?
                 ;; [".{{appname}}/appengine-web.xml.mustache"
                 ["etc/appengine-web.xml.mustache"
                  (render "appengine-web.xml.mustache")]
                 ["etc/web.xml.mustache"
                  (render "web.xml.mustache")]

                 ;; resources install to source tree
                 ;; migae plugin "config" task will copy to war tree

                 ["{{statics_src}}/{{welcome}}"
                  (render "home.html" (conj {:loc "Home"} data))]

                 ["{{statics_src}}/html/{{welcome}}"
                  (render "index.html" (conj {:loc "HTML"} data))]
                 ["{{statics_src}}/404.html"
                  (render "404.html" data)]

                 ["{{statics_src}}/html/a/{{welcome}}"
                  (render "index.html" (conj {:loc "A"} data))]
                 ["{{statics_src}}/html/b/{{welcome}}"
                  (render "index.html" (conj {:loc "B"} data))]
                 ["{{statics_src}}/request/{{welcome}}"
                  (render "index.html" (conj {:loc "Request"} data))]
                 ["{{statics_src}}/user/{{welcome}}"
                  (render "index.html" (conj {:loc "User"} data))]

                 ["{{statics_src}}/css/{{project}}.css"
                  (render "project.css" data)]
                 ["{{statics_src}}/js/{{project}}.js"
                  (render "project.js" data)]
                 ["{{resources_src}}/favicon.ico"
                  (render "favicon.ico" data)]

                 ["etc/{{java-logging}}" (render "logging.properties" data)]
                 ["etc/{{log4j-logging}}" (render "log4j.properties" data)]

                 ;; templates?
                 ;; ["private/hiccup/{{appname}}.???" (render "..." data)]

                 ;; TODO: add a spinner, favicon, or other toy graphic
                 ;; ["public/img/{{appname}}.js" (render "public/img/foo.png" data)]
               )))))

(ns leiningen.new.migae
  (:require  [clojure.java.io :as io]
             [clojure.string :only [join]])
  (:use
        [leiningen.new.templates :only [*dir* project-name
                                        renderer
                                        render-text
                                        multi-segment
                                        sanitize-ns name-to-path
                                        ->files
                                        year]]
        [leiningen.core.main :only [abort]]))


(defn- template-path [name path data]
  (io/file name (render-text path data)))

(defn ->filesx
  "Generate a file with content. path can be a java.io.File or string.
  It will be turned into a File regardless. Any parent directories will
  be created automatically. Data should include a key for :name so that
  the project is created in the correct directory"
  [{:keys [name] :as data} & paths]
  (let [dir (or *dir*
                (-> (System/getProperty "leiningen.original.pwd")
                    (io/file name) (.getPath)))]
;        foo (println (format "->files dir: %s" dir))]
    (if (or *dir* (.mkdir (io/file dir)))
      (doseq [path paths]
        (do ;(println (format "topath %s" (first path)))
            (println (format "  rendered: %s" (template-path dir (first path) data)))
        (if (string? path)
          (do (println (format "mkdirs %s"  (template-path dir path data)))
              (.mkdirs (template-path dir path data)))
          (let [[path content & options] path
                path (template-path dir path data)
                options (apply hash-map options)]
            (.mkdirs (.getParentFile path))
            (io/copy content (io/file path))
            (when (:executable options)
              (.setExecutable path true)))))
      (println (str "Could not create directory " dir
                      ". Maybe it already exists?"))))))




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
               :filterclass (str (name-to-path appname) ".reload_filter")
               :servlets [{:servlet "request",
                           :src (str (name-to-path appname) "/request_servlet"),
                           :ns (str appname ".request-servlet"),
                           :class (str (name-to-path appname) ".request_servlet"),
                           :services [{:service "request",
                                       :url-pattern "/request/*"}]}
                                       ;; :action "GET"
                                       ;; :route "/request/:rqst"
                                       ;; :arg {:var "rqst"}}]}
                          {:servlet "user",
                           :src (str (name-to-path appname) "/user_servlet"),
                           :ns (str appname ".user-servlet"),
                           :class (str (name-to-path appname) ".user_servlet"),
                           :services [{:service "prefs",
                                       :url-pattern "/user/prefs"}
                                      {:service "login",
                                       :url-pattern "/user/login"}]}]
               :display-name (project-name appname)
               :project (project-name appname)
               :projroot (name-to-path appname) ;; foo-bar -> foo_bar
               :aots [{:aot (str appname ".request-servlet")}
                      {:aot (str appname ".user-servlet")}
                      {:aot (str appname ".reload-filter")}]
               :namespace (str appname ".request")
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

      (println "\nGenerating servlets from template")
      (doseq [s (:servlets data)]
        (let [servlet (assoc s
                        :appname (:appname data)
                        :name (:name data)
                        :projroot (:projroot data))
              proj (name-to-path (:appname data))
              toservlet (name-to-path (render-text "src/{{src}}" servlet))
              toimpl (name-to-path (render-text
                                    "src/{{appname}}/{{servlet}}"
                                    servlet))]
;              foo (println (format "foo: %s" toimpl))]
          (binding [*dir* (.getPath (io/file
                                     (System/getProperty
                                      "leiningen.original.pwd")
                                     (:name servlet)))]
            (->files servlet
                     [(str toservlet "-servlet.clj")
                      (render "servlet.clj" servlet)]
                     [(str toimpl "_impl.clj")
                      (render "servlet_impl.clj" servlet)]))))

      (println "\nGenerating other stuff")
      (binding [*dir* (.getPath (io/file (System/getProperty "leiningen.original.pwd") (:name data)))]
        (->files data
                 ;; to file  		from template
                 ["project.clj" (render "project.clj" data)]
                 ["README.md" (render "README.md" data)]
                 ["doc/intro.md" (render "intro.md" data)]
                 [".gitignore" (render "gitignore" data)]

                 ["src/{{projroot}}/reload_filter.clj"
                  (render "reloadfilter.clj" data)]

                 ;; ["src/main/java/com/google/apphosting/utils/security/SecurityManagerInstaller.java" (render "SecurityManagerInstaller.java" data)]

                 ;; NB: treatment of '-', '_', '/', and '.'
                 ;; "lein new migae foo-bar" yields:
                 ;; src:  ns foo-bar.core in  src/foo_bar/core.clj
                 ;; ["src/{{nested-dirs}}.clj" (render "core.clj" data)]

                 ;; ;; test: foo-bar.core-test -> foo_bar/core_test.clj
                 ["test/{{nested-dirs}}_test.clj" (render "core_test.clj" data)]

                 ;; app engine config files
                 ;; copy template w/o macro processing ('data' arg)
                 ;; install in hidden dir?  or etc?
                 ;; [".{{appname}}/appengine-web.xml.mustache"

                 ["etc/appengine-web.xml.mustache" (render "appengine-web.xml.mustache")]
                 ["etc/dir-locals.el.mustache" (render "dir-locals.el.mustache")]
                 ["etc/{{java-logging}}" (render "logging.properties")]
                 ["etc/{{log4j-logging}}" (render "log4j.properties")]
                 ["etc/web.xml.mustache" (render "web.xml.mustache")]

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

                 ["{{statics_src}}/css/{{project}}.css"
                  (render "project.css" data)]
                 ["{{statics_src}}/js/{{project}}.js"
                  (render "project.js" data)]
                 ["{{resources_src}}/favicon.ico"
                  (render "favicon.ico" data)]

                 ;; templates?
                 ;; ["private/hiccup/{{appname}}.???" (render "..." data)]

                 ;; TODO: add a spinner, favicon, or other toy graphic
                 ;; ["public/img/{{appname}}.js" (render "public/img/foo.png" data)]
               )))))

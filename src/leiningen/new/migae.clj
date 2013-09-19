(ns leiningen.new.migae
  (:require  [clojure.java.io :as io]
             [clojure.string :as str])
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

(defn make-pmap
  [args]
  (do
    (let
        [args (first args)
         project (first args)
         [appname appid] (str/split (str project) #":")
         appid (if (not appid)
                 (do (println "missing gae app-id; using appname")
                     appname)
                 appid)
         sdk (second args)
         render (renderer "migae")
         main-ns (multi-segment (sanitize-ns appname))]
      {:name appname ;; ":name" key required by leiningen
       :projdir (str/join "/"
                          [(System/getProperty "leiningen.original.pwd")
                           appname])
       :appname appname
       :_appname (name-to-path appname)
       :version "0.1.0-SNAPSHOT"
       :appid appid
       :gae-app-version "0-1-0"
       :raw-name appname
       :filterclass (str (name-to-path appname) ".reload_filter")
       :servlets [{:-servlet "request",
                   :src (str (name-to-path appname) "/request_servlet"),
                   :ns (str appname ".request-servlet"),
                   :class (str (name-to-path appname) ".request_servlet"),
                   :services [{:service "request",
                               :url-pattern "/request/*"}]}
                  ;; :action "GET"
                  ;; :route "/request/:rqst"
                  ;; :arg {:var "rqst"}}]}
                  {:-servlet "user",
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
       :log4j-logging "log4j.properties"})))

(defn make-servlet
  [cmdname & args]  ;; cmdname = app | servlet
  (let [project (first (first args))
        [projname servlet] (str/split (str project) #":")
        theservlet (if servlet
                     (do ; (println "making servlet " servlet)
                       servlet)
                     (abort
                      (str "Syntax: lein new migae servlet <projname>:<servletname>")))
        ;; TODO: support 'lein new migae servlet <servletname>' from root dir
        projomap (make-pmap args)
        pmap (merge projomap {:-servlet theservlet
                              :_servlet (name-to-path theservlet)
                              :services {
                                         :service theservlet
                                         :url-pattern (str "/" theservlet "/*")}})
;;        foo (println (:servlet pmap))
        sname (pmap :appid)
        render (renderer "migae")
        ;; servlet (assoc s
        ;;           :appname (:appname pmap)
        ;;           :name (:name pmap)
        ;;           :projroot (:projroot pmap))
        proj (name-to-path (:appname pmap))
        tobase (str/join "/"
                            [(if (= cmdname "app") projname ".")
                             (name-to-path (render-text
                                            "src/{{appname}}/{{-servlet}}"
                                            pmap))])]
    (do
      (if (= cmdname "servlet")
        (do
          (println (format
                    "Generating servlet %s in project %s"
                    (pmap :-servlet)
                    (pmap :name)))
          (println "WARNING: you must edit the :servlets section of project.clj")
          (println
           (render-text
            (str "Add this stanza:\n"
                 "{:servlet \"{{-servlet}}\",\n"
                 " :src \"{{_appname}}/{{_servlet}}_servlet.clj\"\n"
                 " :ns \"{{appname}}.{{-servlet}}-servlet\",\n"
                 " :class \"{{_appname}}.{{_servlet}}_servlet\",\n"
                 " :filters [{:filter \"reload_filter\"}],\n"
                 " :services [{:service \"{{-servlet}}\" :url-pattern  \"/{{-servlet}}/*\"}]}\n")
            pmap))))
      (binding [*dir* (.getCanonicalPath (io/file
                                          (System/getProperty
                                           "leiningen.original.pwd")))]
        (->files pmap
                 [(str tobase "_servlet.clj")
                  (render "servlet.clj" pmap)]
                 [(str tobase "_impl.clj")
                  (render "servlet_impl.clj" pmap)])))))

(defn make-app
  [projname & args]
  (if (nil? args)
    (abort (str "missing sdk path!"
                "Syntax:  lein new migae myapp:gae-app-id path/to/sdk"))
    (let [pmap (make-pmap args)
          render (renderer "migae")]
      (println "Generating migae project:"
               (pmap :appname)
               ", app-id:"
               (pmap :appid)
               ", template: migae")

      (make-servlet projname (list (str/join ":" [(pmap :appname) "request"])))
      (make-servlet projname (list (str/join ":" [(pmap :appname) "user"])))

      (binding [*dir* (.getPath
                       (io/file
                        (System/getProperty "leiningen.original.pwd")
                        (:name pmap)))]
        (->files pmap
                 ;; to file  		from template
                 ["project.clj" (render "project.clj" pmap)]
                 ["README.md" (render "README.md" pmap)]
                 ["doc/intro.md" (render "intro.md" pmap)]
                 [".gitignore" (render "gitignore" pmap)]

                 ["src/{{projroot}}/reload_filter.clj"
                  (render "reload_filter.clj" pmap)]

                 ;; ["src/main/java/com/google/apphosting/utils/security/SecurityManagerInstaller.java" (render "SecurityManagerInstaller.java" pmap)]

                 ;; NB: treatment of '-', '_', '/', and '.'
                 ;; "lein new migae foo-bar" yields:
                 ;; src:  ns foo-bar.core in  src/foo_bar/core.clj
                 ;; ["src/{{nested-dirs}}.clj" (render "core.clj" pmap)]

                 ;; ;; test: foo-bar.core-test -> foo_bar/core_test.clj
                 ["test/{{nested-dirs}}_test.clj" (render "core_test.clj" pmap)]

                 ;; app engine config files
                 ;; copy template w/o macro processing ('pmap' arg)
                 ;; install in hidden dir?  or etc?
                 ;; [".{{appname}}/appengine-web.xml.mustache"

                 ["etc/appengine-web.xml.mustache" (render "appengine-web.xml.mustache")]
                 ["etc/dir-locals.el.mustache" (render "dir-locals.el.mustache")]
                 ["etc/logging.properties" (render "logging.properties")]
                 ["etc/log4j.properties" (render "log4j.properties")]
                 ["etc/web.xml.mustache" (render "web.xml.mustache")]
                 ["etc/migae-save-buffer.el" (render "migae-save-buffer.el")]

                 ;; resources install to source tree
                 ;; migae plugin "config" task will copy to war tree

                 ["{{statics_src}}/{{welcome}}"
                  (render "home.html" (conj {:loc "Home"} pmap))]

                 ["{{statics_src}}/html/{{welcome}}"
                  (render "index.html" (conj {:loc "HTML"} pmap))]
                 ["{{statics_src}}/404.html"
                  (render "404.html" pmap)]

                 ["{{statics_src}}/html/a/{{welcome}}"
                  (render "index.html" (conj {:loc "A"} pmap))]
                 ["{{statics_src}}/html/b/{{welcome}}"
                  (render "index.html" (conj {:loc "B"} pmap))]
                 ["{{statics_src}}/request/{{welcome}}"
                  (render "index.html" (conj {:loc "Request"} pmap))]

                 ["{{statics_src}}/css/{{project}}.css"
                  (render "project.css" pmap)]
                 ["{{statics_src}}/js/{{project}}.js"
                  (render "project.js" pmap)]
                 ["{{resources_src}}/favicon.ico"
                  (render "favicon.ico" pmap)]

                 ;; templates?
                 ;; ["private/hiccup/{{appname}}.???" (render "..." pmap)]

                 ;; TODO: add a spinner, favicon, or other toy graphic
                 ;; ["public/img/{{appname}}.js" (render "public/img/foo.png" pmap)]
                 )))))

;; main template entry point
(defn migae
  "A Leiningen template for a new migae project"
  [projname & args]
  ;; projname syntax:  <clj-app>:<gae-app-id>
  (do
    (cond
     (= projname "app") (make-app projname args)
     (= projname "servlet") (make-servlet projname args)
     :else (println "bad cli"))))
                                        ; (make-app projname args))))

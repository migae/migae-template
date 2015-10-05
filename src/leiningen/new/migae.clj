(ns leiningen.new.migae
  (:require  [clojure.java.io :as io]
             [clojure.tools.reader.edn :as edn :only [read read-string]]
             [clojure.set :as set]
             [clojure.string :as string]
             [clojure.pprint :as pp]
             ;; [clojure.core.cache :refer :all]
             [stencil.core :as stencil :refer :all]
             [leiningen.core.user :as user]
             [leiningen.core.main :as main]
             [leiningen.new.templates :refer [*dir* project-name
                                              ;; renderer
                                              render-text
                                              ;; multi-segment
                                              sanitize-ns name-to-path
                                              ->files
                                              year]]
             [leiningen.core.main :refer [abort]]))

(defonce specmap (atom {}))

(defn- template-path [name path data]
  (io/file name (render-text path data)))

(def ^{:dynamic true} *force?* false)

;; templates.clj
(defn ->filesx
  [{:keys [project] :as data} & paths]
  (let [;;foonm (println "PROJECT: " project)
        ;;fooda (println "DATA: " data)
        ;;foopa (println "PATHS: " (first paths))
        ;; log (println "PWD:" (System/getProperty "leiningen.original.pwd"))
        ;; log (println "*dir*" *dir*)
        dir (or *dir*
                (-> (System/getProperty "leiningen.original.pwd")
                    (io/file project)
                    (.getPath)))
        ]
    (if (or (= "." dir) (.mkdir (io/file dir)) *force?*)
      (doseq [path paths]

 ;; (println "DIR: " dir)
 ;; (println "path" path)

        (let [to-path (template-path dir (first path) data)]
          ;; (println (format "first path %s" (first path)))
          ;; (println (format "to path: %s %s" to-path (type to-path)))

          (if (string? path)
            (.mkdirs (template-path dir path data))
            (let [[path content & options] path
                  path (template-path dir path data)
                  options (apply hash-map options)]
              ;; (println "CONTENT: " content)
              ;; (println "OUTPUT: " (io/file path))
              (.mkdirs (.getParentFile path))
              (io/copy content (io/file path))
              (when (:executable options)
                (.setExecutable path true))))))
      (println (str "Could not create directory " dir
                 ". Maybe it already exists?"
                 "  See also :force or --force")))))

(defn slurp-to-lf
  "Returns the entire contents of the given reader as a single string. Converts
  all line endings to \\n."
  [r]
  (let [sb (StringBuilder.)]
    (loop [s (.readLine r)]
      (if (nil? s)
        (str sb)
        (do
          (.append sb s)
          (.append sb "\n")
          (recur (.readLine r)))))))

(defn fix-line-separators
  "Replace all \\n with system specific line separators."
  [s]
  (let [line-sep (if (user/getenv "LEIN_NEW_UNIX_NEWLINES") "\n"
                     (user/getprop "line.separator"))]
    (string/replace s "\n" line-sep)))

(defn slurp-resource
  "Reads the contents of a resource. Temporarily converts line endings in the
  resource to \\n before converting them into system specific line separators
  using fix-line-separators."
  [resource]
  (if (string? resource) ; for 2.0.0 compatibility, can break in 3.0.0
    (-> resource io/resource io/reader slurp-to-lf fix-line-separators)
    (-> resource io/reader slurp-to-lf fix-line-separators)))

(defn sanitize
  "Replace hyphens with underscores."
  [s]
  (string/replace s "-" "_"))

(defn renderer
  "Create a renderer function that looks for mustache templates in the
  right place given the name of your template. If no data is passed, the
  file is simply slurped and the content returned unchanged.

  render-fn - Optional rendering function that will be used in place of the
              default renderer. This allows rendering templates that contain
              tags that conflic with the Stencil renderer such as {{..}}."
  [name & [render-fn]]
  (let [render (or render-fn render-text)]
    (fn [template & [data]]
      ;; (println "render " template " data: " data)
      (let [path (string/join "/" ["leiningen" "new" (sanitize name) template])]
        ;; (println "RENDERER PATH:" path)
        (if-let [resource (io/resource path)]
          (if data
            (render (slurp-resource resource) data)
            (do (println "RESOURCE:" resource)
                (io/reader resource)))
          (main/abort (format "Template resource '%s' not found." path)))))))

(defn render-deps
  [text]
  ;; (println (str "render-deps " text))
  (let [t (render-string text @specmap)]
    ;; (println (str "RENDERED: " t))
    t))
  ;; (let [reqs (edn/read-string text)
  ;;       render (renderer "migae")]
  ;;   (apply str
  ;;     (for [req reqs]
  ;;       (do
  ;;         ;; (println "req: " req (type req))
  ;;         (if (string? req)
  ;;           (str "    " req \newline)
  ;;           (str "    " req \newline)))))))
  ;;         ;; (render-string (str "    " req (when (not (string? req)) \newline)) nil))))))

(defn- base-templates
  [render spec]
  [["README.adoc" (render "README.adoc" spec)]
   ["doc/intro.adoc" (render "intro.adoc" spec)]
   [".gitignore" (render "gitignore" spec)]])
   ;; TODO: parameterize proj subdir

(defn- gradle-templates
  [render spec]
  (println "gradle-templates")
  [["build.gradle" (render "gae/build.gradle.main.mustache" spec)]
   ["settings.gradle" (render "settings.gradle.mustache" spec)]
   ["gradle.properties" (render "gradle.properties" spec)]
   ["gradlew" (render "gradlew" spec) :executable true]
   ["gradlew.bat" (render "gradlew.bat" spec)]
   ["gradle/wrapper/gradle-wrapper.properties"
    (render "gradle-wrapper.properties" spec)]
   ["webapp/build.gradle" (render "gae/build.gradle.project.mustache" spec)]
   ["gradle/wrapper/gradle-wrapper.jar"
    (io/file  "./src/leiningen/new/migae/gradle-wrapper.jar")]])

(defn- resources-templates
  [render spec]
  )

(defn- clj-servlet-templates
  [render spec]
  [["webapp/src/main/clojure/{{#ns.components}}{{component}}/{{/ns.components}}/servlets.clj"
    (render "servlets/servlets.clj.mustache" spec)]
   ["webapp/src/main/clojure/{{#ns.components}}{{component}}/{{/ns.components}}core.clj"
    (render "servlets/core.clj.mustache" spec)]
   ["webapp/src/main/clojure/{{#ns.components}}{{component}}/{{/ns.components}}admin.clj"
    (render "servlets/admin.clj.mustache" spec)]
   ["webapp/src/main/clojure/{{#ns.components}}{{component}}/{{/ns.components}}reloader.clj"
    (render "servlets/reloader.clj.mustache" spec)]])

(defn- gae-cljs-templates
  [render spec]
  (println "cljs-templates")
  [["webapp/project.clj" (render "cljs/project.clj.mustache" spec)]
   ["webapp/src/main/cljs/{{#ns.components}}{{component}}/{{/ns.components}}core.cljs"
    (render "cljs/core.cljs" spec)]
   ["webapp/src/main/cljs/{{#ns.components}}{{component}}/{{/ns.components}}connect.cljs"
    (render "cljs/connect.cljs" spec)]])

(defn- gae-templates
  [render spec]
  [["webapp/fswatch.sh" (render "fswatch.sh" spec) :executable true]
   ["webapp/resources/logging.properties" (render "logging.properties" spec)]
   ["webapp/src/main/webapp/WEB-INF/classes/log4j.properties" (render "log4j.properties" spec)]
   ;; ["webapp/src/main/webapp/index.html" (render "index.html" spec)]
   ["webapp/src/main/webapp/WEB-INF/appengine-web.xml"
    (render "xml.web-appengine.mustache"spec)]
   ["webapp/src/main/webapp/WEB-INF/web.xml" (render "xml.web.mustache" spec)]

   ;; ["{{statics_src}}/html/{{welcome}}"
   ;;  (render "index.html" (conj {:loc "HTML"} spec))]
   ["webapp/src/main/webapp/404.html"
    (render "404.html" spec)]

   ;; ["{{statics_src}}/html/a/{{welcome}}"
   ;;  (render "index.html" (conj {:loc "A"} spec))]
   ;; ["{{statics_src}}/html/b/{{welcome}}"
   ;;  (render "index.html" (conj {:loc "B"} spec))]
   ;; ["{{statics_src}}/request/{{welcome}}"
   ;;  (render "index.html" (conj {:loc "Request"} spec))]

   ["webapp/src/main/webapp/css/{{#ns.components}}{{component}}/{{/ns.components}}core.css"
    (render "core.css" spec)]

   ["webapp/src/main/webapp/js/{{#ns.components}}{{component}}/{{/ns.components}}core.js"
    (render "core.js" spec)]
   ["webapp/src/main/webapp/favicon.ico"
    (render "favicon.ico" spec)]

   (when (:cljs spec)
     (do
       ["webapp/src/main/cljs/{{#ns.components}}{{component}}/{{/ns.components}}core.cljs"
        (render "cljs/core.cljs" spec)]
       ["webapp/src/main/cljs/{{#ns.components}}{{component}}/{{/ns.components}}connect.cljs"
        (render "cljs/connect.cljs" spec)]))

   ["webapp/test/clojure/core_test.clj" (render "core_test.clj" spec)]
   ])

(defn- jetty-templates
  [render spec]
  (println "jetty-templates")
  (let [v [["project.clj" (render "jetty/project.clj.mustache" spec)]
           ["src/log4j.properties" (render "jetty/log4j.properties.mustache" spec)]
           (if (:polymer spec)
             (do (println "POLYMER")
                 ["src/clj/{{#ns.components}}{{component}}/{{/ns.components}}/{{project}}/core.clj"
                  (render "polymer/core.clj.mustache" spec)])
             (do (println "NOT POLYMER")
                 ["src/clj/{{#ns.components}}{{component}}/{{/ns.components}}/{{project}}/core.clj"
                  (render "clj/core.clj.mustache" spec)]))
           ["resources/public/404.html" (render "404.html" spec)]
           ["test/{{#ns.components}}{{component}}/{{/ns.components}}/{{project}}/core_test.clj"
            (render "jetty/core_test.clj" spec)]
           ["resources/public/styles/{{#ns.components}}{{component}}/{{/ns.components}}core.css"
            (render "core.css" spec)]]]
    (apply
      merge v
      (if (:cljs spec)
        (do ;; clojurescript
          [["src/cljs/{{#ns.components}}{{component}}/{{/ns.components}}core.cljs"
            (render "cljs/core.cljs" spec)]
           ["src/cljs/{{#ns.components}}{{component}}/{{/ns.components}}connect.cljs"
            (render "cljs/connect.cljs" spec)]])
        (do ;; plain javascript
           [["resources/public/scripts/{{#ns.components}}{{component}}/{{/ns.components}}core.js"
            (render "core.js" spec)]])))
    ))

(defn make-webapp
  [spec]
  (let [spec (into {} (map (fn [[k v]]
                             (if (= v true)
                               {k render-deps}
                               {k v}))
                        spec))
        render (renderer "migae")
        here (System/getProperty "leiningen.original.pwd")
        cwd (.getPath
                (io/file
                 here "src/leiningen/new/migae"))]
    ;; (pp/pprint "FINAL SPEC:")
    ;; (pp/pprint spec)
    (println "Generating migae project:"
             (spec :project))
             ;; ", gae app id:"
             ;; (spec :gid))
    ;; (println "HERE: " here)
    ;; (println "CWD: " cwd)
    ;; (println "*dir*: " *dir*)

    ;; (binding [*dir* (.getPath
    ;;                  (io/file
    ;;                   here
    ;;                   (:project spec)))]

                          ;; to file  		from template
    (let [templates (vec (concat
                           (base-templates render spec)
                           (cond
                             (= (:platform spec) :gae)
                             (let [res (vec
                                         (gradle-templates render spec)
                                         (gae-templates render spec)
                                         (clj-servlet-templates render spec))
                                   log (pp/pprint (pr-str "RES: " (type res)))]
                               res)
                             (= (:platform spec) :jetty)
                             (do
                               (jetty-templates render spec)))))]
                           ;; (when (some #{:cljs} (:features spec))
                           ;; (if (:cljs spec)
                           ;;   (cljs-templates render spec))))]
          (apply ->filesx spec templates))))

(defn- vet-demo
  [args]
  (println ":demo option not yet implemented" args)
  (System/exit -1)
  )

(defn- vet-testing
  [args]
  (println ":testing option not yet implemented" args)
  (System/exit -1)
  )

(defn- vet-platform
  [args]
  (println "vet-platform")
  (let [kw (first args)
        err-msg (str "platform " kw " not yet supported")]
    (cond
      (= kw :gae)
      [{:platform :gae} (next args)]
      (= kw :jetty)
      [{:platform :jetty} (next args)]
      :else
      (do
          (println err-msg)
          (System/exit -1)))))

(defn- vet-cljs
  [args]
  ;; (println "vet-cljs" args)
  (let [kw (first args)
        requested-deps (second args)
        supported-deps {:dom #{:domina :dommy :hipo}
                        :html #{:sablono :kioo :hipo :enfocus :hickory}
                        :routing #{:silk :bidi :secretary}
                        :testing #{:cljs.test :speclj :purnam.test :clairvoyant}}
        err-msg "option :cljs must be followed by an options map or nothing"
        err-msg-deps (str "bad dependency; allowed are: " supported-deps)]
    ;; (println "requested-deps: " requested-deps)
    (cond
      (map? requested-deps)
      (let [rdep-keys (set (keys requested-deps))
            sdep-keys (set (keys supported-deps))]
        (println "rdep-keys: " rdep-keys)
        (println "sdep-keys: " sdep-keys)
        ;; (if (every? #(some #{%} (set sdep-keys)) rdep-keys)
        (if (set/subset? rdep-keys sdep-keys)
          (do
            (println "RDEP-KEYS OK " requested-deps)
            ;; (let [res (into {}
            (doseq [[k v] requested-deps]
              ;; (cond
              ;;   (= k :dom) ;; vet :dom vals
                (do (println "FOO " (k supported-deps) v)
                    (when (not (contains? (k supported-deps) v))
                      (do (println "foo" err-msg-deps)
                          (System/exit -1)))))
                ;; (= k :foo)
                ;; (do (println "FOOEY " (:dom supported-deps) v)
                ;;     (when (not (contains? (:foo supported-deps) v))
                ;;       (do (println "fooey" err-msg-deps)
                ;;           (System/exit -1))))
                ;; :else (do (println "fooey" err-msg-deps)
                ;;           (System/exit -1))))
            [{kw requested-deps} (nnext args)])
          (do (println "1" err-msg)
              (System/exit -1))))

      (nil? requested-deps) [{kw {}} (nnext args)]
      (not (keyword? requested-deps)) (do (println err-msg) (System/exit -1))
      :else [{kw {}} (next args)])))

(defn- vet-features
  [args]
  ;; (println "vet-features")
  (let [supported-features #{:hiccup :polymer}
        kw (first args)
        requested-features (second args)
        err-msg "option :features must be followed by non-empty vector of keywords"]
    (cond
      (not (vector? requested-features)) (do (println err-msg) (System/exit -1))
      (empty? requested-features) (do (println err-msg) (System/exit -1))
      :else
      (if (every? #(contains? supported-features %) requested-features)
        (let [fs (into {}
                  (for [feature requested-features]
                    (cond
                      (= feature :hiccup) {:dep "compile 'hiccup:hiccup:1.0.5'" }
                      :else {feature true})))]
              [{:hiccup 'render-deps} (nnext args)])
        ;; [{kw requested-features} (nnext args)]
        (do
          (println "unrecognized feature(s): "
            (pr-str (vec (filter #(not (contains? supported-features %)) requested-features))))
          (System/exit -1))))))

(defn- vet-modules
  [args]
  ;; (println "vet-modules")
  (let [kw (first args)
        modules (second args)
        err-msg "option :modules must be followed by non-empty vector of module names"]
      ;; (println "modules:" modules (type modules) (vector? modules))
      (cond
        (not (vector? modules)) (do (println err-msg) (System/exit -1))

        (empty? modules) (do (println err-msg) (System/exit -1))

        :else
        (if (every? #(or (string? %)
                       (keyword? %)
                       (symbol? %))
              modules)
          [{kw modules} (nnext args)]
          ;; (recur (assoc opts arg1 modules) (nnext args))
          (do
            (println "module names must be strings or keywords or symbols: "
              (pr-str (vec (filter
                             #(not (or (string? %)
                                     (keyword? %)
                                     (symbol? %)))
                             modules))))
            (System/exit -1))))))

(defn- vet-polymer
  [args]
  (println "vet-polymer" args)
  (let [kw (first args)
        requested-options (second args)
        supported-options #{:js} ;;  :clj :iron #{:list :ajax :pages}}
        err-msg ":polymer options must be non-empty vector of options"
        err-msg-bad-option "illegal :polymer option"]
    (pp/pprint (str "requested polymer options: " requested-options))
      (cond
        (not (vector? requested-options))
        [{kw true} (next args)]

        ;; (empty? requested-options) (do (println err-msg) (System/exit -1))

        :else
        (if (every? #(contains? supported-options %) requested-options)
        [{kw requested-options} (nnext args)]
        (do
          (println (format "%s: %s" err-msg-bad-option
                     (pr-str (filter #(not (contains? supported-options %)) requested-options))))
          (System/exit -1))))))

(defn- vet-services
  [args]
  ;; (println "services opt")
  (let [kw (first args)
        requested-services (second args)
        supported-services #{:datastore :memcache :taskqueue :drawbridge}
        err-msg  "option :services must be followed by non-empty vector of keywords"]
    ;; (println "requested services: " services)
    (cond
      (not (vector? requested-services)) (do (println err-msg) (System/exit -1))
      (empty? requested-services) (do (println err-msg) (System/exit -1))
      (not (every? keyword? requested-services)) (do (println err-msg) (System/exit -1))

      :else
      (if (every? #(contains? supported-services %) requested-services)
        [{kw requested-services} (nnext args)]
        ;; (recur (assoc opts arg1 requested-services) (nnext args))
        (do
          (println "unrecognized service(s): "
            (pr-str (vec (filter #(not (contains? supported-services %)) requested-services))))
          (System/exit -1))))))

;; TODO: offer options for standard components, e.g. we might have
;;  :datastore :dataomic, :postgres, :gae, :redis, etc.

(def web-components
  #{:ring :ringx :compojure :compojure-api :schema :async :polymer})

(def supported-platforms #{:gae :jetty})

(defn- parse-args
  [& args]
  (println "parsing " args)
  (let [[opts args]
        (loop [opts {}, args args]
          (let [arg1 (first args)]
            ;; (println "opts: " opts)
            ;; (println "args: " args)
            ;; (println "arg1: " arg1)

            (condp #(%1 %2) arg1

              #(contains? #{:gid} %)
              (do (println "ids opts")
                  (recur (assoc opts arg1 (second args)) (nnext args)))

              #(= :ns %)
              (do #_(println ":ns")
                  (let [ns (second args)
                        cs (string/split (str ns) #"\.")
                        components (into []
                                     (for [c cs]
                                       {:component c}))]
                    ;; (println "ns: " ns)
                    ;; (println "components: " components)
                    (recur (merge opts {:ns {:sym ns
                                             :components components}})
                             (nnext args))))

              #(= :cljs %)
              (let [[entry next-args] (vet-cljs args)]
                (recur (merge opts entry) next-args))

              #(= :demo %)
              (let [[entry next-args] (vet-demo args)]
                (recur (merge opts entry) next-args))

              ;; #(= :features %)
              ;; (let [[entry next-args] (vet-features args)]
              ;;   (recur (merge opts entry) next-args))

              ;; TODO: prohibit namespaced keywords like :a/b
              #(= :modules %)
              (let [[entry next-args] (vet-modules args)]
                (recur (merge opts entry) next-args))

              #(= :polymer %)
              (let [[entry next-args] (vet-polymer args)]
                (recur (merge opts entry) next-args))

              #(= :services %)
              (let [[entry next-args] (vet-services args)]
                (recur (merge opts entry) next-args))

              ;; :midje, :speclj, :clojure.test
              #(= :testing %)
              (let [[entry next-args] (vet-testing args)]
                (recur (merge opts entry) next-args))

              #(contains? supported-platforms %)
              (recur (assoc opts :platform arg1) (next args))

             #(contains? web-components %)
              (do (println "flavor opt" arg1)
                  (recur (assoc opts arg1 true) (next args)))
              ;; (do
              ;;   (cond
              ;;     (= % :compojure)
              ;;     (recur (assoc opts :compojure "\n\t[compojure.core :refer :all]") (next args))


              nil?
              (do #_(println "NIL")
                  #_(println "returning: [" opts args "]")
                  [opts (merge args {})])

              (fn [any] (do (print "unrecognized option:" any) true))
              (do
                (cond
                  (= arg1 :module)
                  (println " -- did you mean :modules?")
                  (= arg1 :service)
                  (println " -- did you mean :services?")
                  :else
                  (println))
                (System/exit -1))
              )))]
    (pp/pprint (str "opts: " opts))
;;    (pp/pprint (str "merged opts: " (merge opts {})))
    (merge opts {})))

(defonce default-map
  {:platform :jetty
   :project-version "0.1.0-SNAPSHOT"
   :app-version "v0-1-0-snapshot"
   ;; app-version: no uppercase; Google recommends starting with alpha; see
   ;; https://cloud.google.com/appengine/docs/java/config/appconfig?hl=en#Java_appengine_web_xml_About_appengine_web_xml
   :ns {:sym nil
        :components nil}
   :clojure true
   :compojure true
   :ring true
   :hiccup true
   :servlets [{:url-pattern "/*"
               :servlet "core"
               :description "this is the core implementation"}
              {:url-pattern "/admin/*"
               :servlet "admin"
               :description "this is the admin implementation"
               :security [{:url-pattern "/admin/*"
                           :web-resource-name "admin"
                           :role-name "admin"}]}]})

;; main template entry point
(defn migae
  "A Leiningen template for a new migae project"
  ([project & args]
   (println "project: " project "args: " args)
   (stencil.loader/set-cache {})
   (let [argstr (clojure.string/join " " args)
         args (edn/read-string (str \( argstr \)))
         opts (apply parse-args args)
log (println "parsed opts: " (str opts))
         opts (if (= (:platform opts) :jetty)
                (merge opts {:ring true, :ringx true})
                opts)
log (println "opts: " (str opts))
         default (into default-map {:project project
                                    :name project ;; required by leiningen implementation
                                    :ns {:sym project :components project}
                                    :app-id (str project "-id")})
         spec (merge default opts)
         spec (if (= (:platform spec) :jetty)
                   (dissoc spec :servlets)
                   spec)]
     (println "\nSPEC:")
     (pp/pprint spec)
     (print "\nProceed? [Y/n] ")(flush)
     (reset! specmap spec)
     (let [ok (read-line)]
       ;; (pp/pprint (str "ok: [" ok "] " (type ok)))
       (when (or (empty? ok) (= ok "y") (= ok "Y"))
         (make-webapp spec))))))

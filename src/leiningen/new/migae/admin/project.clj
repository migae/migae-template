(defproject {{name}}/admin "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :min-lein-version "2.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url http://example.com/FIXME

  ;; UNCOMMENT to disable security checks:
  :java-source-paths ["src/main/java"]
  :javac-options ["-nowarn" "-target" "1.7" "-source" "1.6" "-Xlint:-options"]
  :resource-paths ["war/WEB-INF/classes/"
                   "war/WEB-INF/lib/*"
                   "/usr/local/java/appengine/lib/shared/*"]
  :jvm-opts ["-javaagent:/usr/local/java/appengine/lib/agent/appengine-agent.jar"
             "-Xbootclasspath/p:war/WEB-INF/lib/appengine-dev-jdk-overrides.jar"
             "-D--startOnFirstThread" ;; if os x
             "-Ddatastore.auto_id_allocation_policy=scattered"
             "-Dappengine.sdk.root=/usr/local/java/appengine"
             "-D--property=kickstart.user.dir=lexrest"
             "-D--enable_all_permissions=true"
             "-Djava.awt.headless=true"]

  :repl-options {:port 4005
                 :init (do
                         (import 'com.google.appengine.tools.development.DevAppServerMain)
                         (defn jetty []
                           (do (println "launching appengine-magic dev server")))
                         (defn gserver []
                           (do (println "launching GAE DevAppServer")
                               (DevAppServerMain/main
                                (into-array String
                                            [;;"--address=localhost"
                                             ;;"--port=8082"
                                             "--sdk_root=war/WEB-INF/sdk"
                                             "--disable_update_check"
                                             "--property=kickstart.user.dir=lexrest"
                                             "war"])))))
                 }
  :gae-sdk "/usr/local/java/appengine"
  :gae-app {:id "arabiclexicon"
            ;; using '-' prefix on version nbr forces user to customize
            :version  {:dev "0-50-0"
                       :test "-0-1-0"
                       :prod "-0-1-0"}
            :servlets [{:name "lex", :class "admin",
                        :services [{:svcname "admin" :url-pattern  "/admin"}
                                    {:svcname "upload" :url-pattern  "/u"}
                                   ]}
                        {:name "lex", :class "user",
                         :services [{:svcname "profile" :url-pattern  "/profile/*"}
                                    {:svcname "login"
                                     :url-pattern  "/_ah/login_required"}
                                    {:svcname "logout" :url-pattern  "/logout"}
                                    ]}
                       ]
            :war "war"
            :display-name "lexrest"
            :welcome "index.html"
            :threads true,
            :sessions true,
            :java-logging "logging.properties",
            ;; static-files: html, css, js, etc.
            :statics {:src "src/main/public"
                      :dest ""
                      :include {:pattern "public/**"
                                ;; :expire "5d"
                                }
                      ;; :exclude {:pattern "foo/**"}
                      }
            ;; resources: img, etc. - use lein default
            :resources {:src "src/main/resource"
                        :dest ""
                        :include {:pattern "public/**"
                                  ;; :expire "5d"
                                  }
                        ;; :exclude {:pattern "bar/**"}
                        }
            }
  :aot [#"lex.*"]
  :compile-path "war/WEB-INF/classes"
  :target-path "war/WEB-INF/lib"
  :keep-non-project-classes false
  :omit-source true ;; default
  :jar-exclusions [#"^WEB-INF/appengine-generated.*$"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 ;; NB: plain ring won't work
                 [ring/ring-core "1.1.8"]
                 [ring/ring-devel "1.1.8"]
                 [compojure "1.1.5"]
                 [commons-fileupload/commons-fileupload "1.3"]
                 ;; compojure dependencies must be included here?
                 [org.clojure/core.incubator "0.1.0"]
                 [org.clojure/tools.macro "0.1.0"]
                 [clout "1.1.0"]        ; why do we need this?
                 ;; [org.clojure/core.incubator "0.1.2"]
                 [org.clojure/math.combinatorics "0.0.3"]
                 [appengine-magic/kernel "0.6.0-SNAPSHOT"]
                 [appengine-magic.service/magic-user "0.6.0-SNAPSHOT"]
                 [org.sibawayhi.lex/lex-user "0.1.0-SNAPSHOT"]
                 [hiccup "1.0.2"]
                 [commons-codec "1.7"]
                 [org.clojure/tools.logging "0.2.6"]]
  ;;  :profiles {:dev {:dependencies [[]]}}
  :plugins [[lein-magic "0.3.0-SNAPSHOT"]])

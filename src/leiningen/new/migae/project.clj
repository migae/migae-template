(defproject {{appname}} "{{version}}"
  :description "FIXME: write description"
  :min-lein-version "2.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "{{proj-url}}"

  :gae-sdk "{{sdk}}"
  :gae-app {:id "{{gae-app-id}}"
            ;; GAE version ID
            ;; using '-' prefix on version nbr forces user to customize
            :version  {:dev "-{{gae-app-version}}"
                       :test "-{{gae-app-version}}"
                       :prod "-{{gae-app-version}}"}
            :servlets [{{#servlets}}{:name "{{name}}", :class "{{class}}",
                       :services [{{#services}}{:svcname "{{svcname}}" :url-pattern  "{{url-pattern}}"}
                                  {{/services}}]}
                       {{/servlets}}]
            :war "{{war}}"
            :display-name "{{display-name}}"
            :welcome "{{welcome}}"
            :threads {{threads}},
            :sessions {{sessions}},
            :java-logging "{{java-logging}}",
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
  :aot [{{#aots}}{{aot}} {{/aots}}]
  :resource-paths ["src/"]
  :compile-path "{{war}}/WEB-INF/classes"
  :target-path "{{war}}/WEB-INF/lib"
  :libdir-path "{{war}}/WEB-INF/lib"
  :jar-exclusions [#".*impl*" #"^WEB-INF/appengine-generated.*$"]
  :clean-targets [:compile-path :target-path]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring/ring-servlet "1.2.0"]]
                 ;; [migae/migae-env "0.1.0-SNAPSHOT"]
                 ;; [migae/migae-blobstore "0.1.0-SNAPSHOT"]
                 ;; [migae/migae-channel "0.1.0-SNAPSHOT"]
                 ;; [migae/migae-datastore "0.1.0-SNAPSHOT"]
                 ;; [migae/migae-images "0.1.0-SNAPSHOT"]
                 ;; [migae/migae-mail "0.1.0-SNAPSHOT"]
                 ;; [migae/migae-memcache "0.1.0-SNAPSHOT"]
                 ;; [migae/migae-taskqueues "0.1.0-SNAPSHOT"]
                 ;; [migae/migae-urlfetch "0.1.0-SNAPSHOT"]
                 ;; [migae/migae-user "0.1.0-SNAPSHOT"]
  :profiles {:dev {:plugins [[lein-migae "0.1.3"]
                             [lein-libdir "0.1.1"]]}})

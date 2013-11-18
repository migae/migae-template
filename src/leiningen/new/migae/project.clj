(defproject {{appname}} "{{version}}"
  :description "FIXME: write description"
  :min-lein-version "2.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "{{proj-url}}"

  :migae {;; :sdk "{{sdk}}"
          ;; set :sdk here or in ~/.lein/profiles.clj as follows:
          ;;    {:user {:migae {:sdk ...}}}
          :devlog "devserver.log" ;; for 'lein migae run' stdout/stderr
          ;; GAE appliclation and version IDs
          ;; using '-' prefix on version nbr forces user
          ;; to customize before first deploy
          :version  {:dev ["{{appid}}" "-{{gae-app-version}}"]
                     :beta ["{{appid}}-beta" "-{{gae-app-version}}"]
                     :prod ["{{appid}}" "-{{gae-app-version}}"]}
          :filters [{:filter "reload_filter"
                     :ns "{{name}}.reload-filter"
                     :class "{{filterclass}}"}]
          :servlets [{{#servlets}}{:servlet "{{-servlet}}",
                      :src "{{src}}.clj"
                      :ns "{{ns}}",
                      :class "{{class}}",
                      :filters [{:filter "reload_filter"}]
                      :services [{{#services}}{:service "{{service}}" :url-pattern  "{{url-pattern}}"}
                                              {{/services}}]}
                     {{/servlets}}]
          ;; :security [{:url-pattern "/*"
          ;;            :web-resource-name "foo"
          ;;            :role-name "*"}]
          :war "{{war}}"
          :display-name "{{display-name}}"
          :welcome "{{welcome}}"
          :threads {{threads}},
          :sessions {{sessions}},
          :logging [:jul :slf4j]
          ;; static-files: html, css, js, etc.
          :statics {:src "resources/public"
                    :dest ""
                    :include {:pattern "public/**"
                              ;; :expire "5d"
                              }
                    ;; :exclude {:pattern "foo/**"}
                    }
          ;; resources: img, etc. - use lein default
          :resources {:src "resources"
                      :dest ""
                      :include {:pattern "public/**"
                                ;; :expire "5d"
                                }
                      ;; :exclude {:pattern "bar/**"}
                      }
          }
  :aot [#".*"]
  ;; :aot [#".*servlet" #".*filter"]
  :source-paths ["src/clj" "src/cljs"]
  ;; :resource-paths ["src/"]
  :web-inf "{{war}}/WEB-INF"
  :compile-path "{{war}}/WEB-INF/classes"
  :target-path "{{war}}/WEB-INF/lib"
  :libdir-path "{{war}}/WEB-INF/lib"
  :jar-exclusions [#".*impl*" #"^WEB-INF/appengine-generated.*$"]
  :clean-targets [:web-inf]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2030"]
                 [compojure "1.1.5"]
                 [ring/ring-servlet "1.2.0"]
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
                 [log4j "1.2.17" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [org.slf4j/slf4j-log4j12 "1.6.6"]
                 [org.clojure/tools.logging "0.2.3"]]
  :profiles {:dev {:plugins [[lein-migae "0.1.6-SNAPSHOT"]
                             [lein-libdir "0.1.1"]
                             [lein-cljsbuild "1.0.0-alpha2"]]}}
  ;; cljsbuild tasks configuration
  :cljsbuild {:builds
              {:debug
               {:source-paths ["src/cljs"]
                :compiler {:output-to "{{war}}/js/{{appname}}/core.js"
                           :optimizations false
                           :pretty-print true}}
              :dev
               {:source-paths ["src/cljs"]
                :compiler {:output-to "{{war}}/js/{{appname}}/core.js"
                           :optimizations :whitespace
                           :pretty-print true}}}
              :prod
              {:source-paths ["src/cljs"]
               :compiler {:output-to "{{war}}/js/{{appname}}/core.js"
                          :optimizations :advanced
                          :pretty-print false}}
              :test
              ;; This build is for the ClojureScript unit tests that will
              ;; be run via PhantomJS.  See the phantom/unit-test.js file
              ;; for details on how it's run.
              {:source-paths ["src/cljs" "test/cljs"]
               :compiler {:output-to "{{war}}/js/{{appname}}/core.js"
                          :optimizations :whitespace
                          :pretty-print true}}})

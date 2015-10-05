(defproject {{appname}} "{{version}}"
  :description "FIXME: write description"
  :min-lein-version "2.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "http://example.com/FIXME"

  :aot [#".*"]
  ;; :aot [#".*servlet" #".*filter"]
  :source-paths ["src/clj"]
  ;; :resource-paths ["src/"]
  :web-inf "{{war}}/WEB-INF"
  :compile-path "{{war}}/WEB-INF/classes"
  :target-path "{{war}}/WEB-INF/lib"
  :libdir-path "{{war}}/WEB-INF/lib"
  :jar-exclusions [#".*impl*" #"^WEB-INF/appengine-generated.*$"]
  :clean-targets [:web-inf]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2030"
                  :exclusions [org.apache.ant/ant]]
                 [compojure "1.1.5"]
                 [ring/ring-servlet "1.2.0"]
                 [log4j "1.2.17" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [org.slf4j/slf4j-log4j12 "1.6.6"]
                 [org.clojure/tools.logging "0.2.3"]]
  :profiles {:dev {:plugins [[lein-migae "0.1.6-SNAPSHOT"]
                             [lein-libdir "0.1.1"]
                             [lein-cljsbuild "1.0.0-alpha2"]]}}
  :hooks [leiningen.cljsbuild]
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

(defproject org.mobileink.migae/lein-template "0.5.0-SNAPSHOT"
  :description "migae project template"
  :url "http://github.com/mobileink/migae-template"
  :source-paths ["src" "src/main/clojure"]
;  :aot [#".*migae.*"]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/tools.reader "0.8.16"]]
;;                 [org.clojure/core.cache "0.6.4"]
                 ;; [stencil "0.5.0" :exclude [org.clojure/core.cache]]]
  :eval-in-leiningen true)

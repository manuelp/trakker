(defproject
  trakker
  "0.2.0-SNAPSHOT"
  :dependencies
  [[org.clojure/clojure "1.5.1"]
   [lib-noir "0.7.1"]
   [compojure "1.1.5"]
   [ring-server "0.3.0"]
   [selmer "0.4.6"]
   [com.taoensso/timbre "2.6.2"]
   [com.postspectacular/rotor "0.1.0"]
   [com.taoensso/tower "1.7.1"]
   [markdown-clj "0.9.33"]
   [com.h2database/h2 "1.3.173"]
   [korma "0.3.0-RC5"]
   [log4j
    "1.2.17"
    :exclusions
    [javax.mail/mail
     javax.jms/jms
     com.sun.jdmk/jmxtools
     com.sun.jmx/jmxri]]
   [clj-time "0.6.0"]
   [incanter/incanter-core "1.5.4"]
   [incanter/incanter-charts "1.5.4"]
   [incanter/incanter-svg "1.5.4"]]
  :ring
  {:handler trakker.handler/war-handler,
   :init trakker.handler/init,
   :destroy trakker.handler/destroy}
  :profiles
  {:production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}},
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.2.0"]
                        [org.clojure/tools.namespace "0.2.4"]]
         :source-paths ["dev"]}}
  :url "https://github.com/manuelp/trakker"
  :plugins
  [[lein-ring "0.8.5"]]
  :description "Web based time tracker."
  :license {:name "Mozilla Public License 2.0"
            :url "http://www.mozilla.org/MPL/2.0/"}
  :min-lein-version "2.0.0")

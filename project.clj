(defproject pow-blockchain "0.1.0-SNAPSHOT"
  :description "Proof-of-Work Blockchain Sample"
  :url "https://vutr.io"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [digest "1.4.8"]
                 [ring/ring-json "0.4.0"]
                 [clj-http "3.9.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler blockchain.core/app :open-browser? false :port 3000}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})

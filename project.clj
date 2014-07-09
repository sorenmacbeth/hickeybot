(defproject hickeybot "0.1.0-SNAPSHOT"
  :description "A clojure aware slack bot"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [cheshire "5.3.1"]
                 [compojure "1.1.6"]
                 [ring/ring "1.2.2"]
                 [clj-http "0.9.2"]
                 [jackknife "0.1.7"]
                 [environ "0.5.0"]]
  :profiles {:uberjar {:aot :all}}
  :main hickeybot.handler)

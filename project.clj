(defproject le-crap "0.3.0"
  :description "Patcher for the Len'en Project"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :profiles {:dev
             {:dependencies [[proto-repl "0.3.1"]]}}
  :aot [le-crap.core]
  :main le-crap.core)

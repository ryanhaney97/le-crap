(defproject le-crap "0.1.0"
  :description "Patcher for the Len'en Project"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [enlive "1.1.5"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :aot [le-crap.core]
  :main le-crap.core)

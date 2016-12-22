(ns le-crap.core
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [le-crap.write-script :refer [data->script]]
            [le-crap.write-data :refer [script->data]]
            [le-crap.util :refer [slurp-jp spit-jp update-map-keys update-map-vals]])
  (:import
   [java.io File])
  (:gen-class))

(def config-file (io/file "./config.edn"))

(def config (atom {}))

(defn read-config [config-file]
  (if (not (.exists config-file))
    (do
     (println "Config file not found! Generating a new one...")
     (spit config-file "{:data-path \"./data\"
                         :script-path \"./scripts\"}")
     (println "Finished generating new config file! Exiting now...")
     (System/exit 0))
    (do
     (println "Reading config file...")
     (reset! config (edn/read-string (string/replace (slurp config-file) #"\/" File/separator))))))

(defn obtain-data-files [path]
  (let [all-files (file-seq (io/file path))
        is-text-file? (fn [file]
                        (= "txt" (last (string/split (str file) #"\."))))
        text-files (sort (filter is-text-file? all-files))
        name-without-numbers (fn [file]
                               (str (apply str (interpose "_" (butlast (string/split (str file) #"_")))) ".txt"))
        grouped-files (group-by name-without-numbers (filter (comp (partial not= "") #(apply str (filter (fn [c] (Character/isDigit c)) (str %1)))) text-files))
        only-numbers-from-name (fn [file]
                                 (Integer/parseInt (apply str (filter #(Character/isDigit %1) (last (string/split (str file) #"_"))))))
        sorted-files (update-map-vals (partial sort-by only-numbers-from-name) grouped-files)
        data-files (update-map-keys #(last (string/split %1 (re-pattern File/separator))) sorted-files)
        data-files (update-map-keys #(string/replace %1 #"_\.txt" ".txt") data-files)]
    data-files))

(defn read-to-scripts [data-path script-path]
  (let [data (obtain-data-files data-path)]
    (dorun (map #(data->script (val %1) (str script-path File/separator (key %1))) data))))

(defn write-to-data [data-path script-path]
  (let [data (obtain-data-files data-path)]
    (dorun (map #(script->data (str script-path File/separator (key %1)) (val %1)) data))))

(defn missing-scripts? [data-path script-path]
  (let [script-files (mapv (comp io/file (partial str script-path File/separator)) (keys (obtain-data-files data-path)))]
    (reduce #(if (.exists %2)
               false
               (reduced true)) false script-files)))

(defn -main []
  (read-config config-file)
  (if (missing-scripts? (:data-path @config) (:script-path @config))
    (do
     (println "Some (or all) script files are missing! Generating script files...")
     (read-to-scripts (:data-path @config) (:script-path @config))
     (println "Finished!"))
    (do
     (println "All script files found! Applying translations...")
     (write-to-data (:data-path @config) (:script-path @config))
     (println "Finished!"))))

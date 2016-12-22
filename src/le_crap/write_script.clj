(ns le-crap.write-script
  (:require [clojure.string :as string]
            [le-crap.read-data :refer [read-all-data]]
            [le-crap.util :refer [spit-jp]]))

(defn format-script [content-map]
  (let [dialogue (:dialogue content-map)
        character-length (+ (count (:character content-map)) 5)
        result (string/replace (string/replace dialogue #"((?<!@),)" (apply str "\n" (repeat character-length " "))) #":" "::")]
    (assoc content-map :dialogue (string/replace result "@," ","))))

(defn assemble-script [content-map]
  (str (apply str ":" (interpose ": " (vals (select-keys content-map [:character :dialogue])))) "\n"))

(def create-script (comp assemble-script format-script))

(def create-scripts (comp (partial apply str) (partial map create-script)))

(defn write-script [file-name content-maps]
  (spit-jp file-name (create-scripts content-maps)))

(defn data->script [sources destination]
  (write-script destination (read-all-data sources)))

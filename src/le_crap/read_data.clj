(ns le-crap.read-data
  (:require
   [clojure.string :as string]
   [le-crap.util :refer [slurp-jp]]))

(defn get-data [file]
  (butlast (string/split (slurp-jp file) #"\n")))

(defn split-data [data]
  (map #(string/split %1 #"((?<!@),)") data))

(defn make-content-map [data]
  (let [data (mapv string/trim data)
        data (mapv #(if (empty? %1)
                      " "
                      %1) data)]
    (zipmap [:cid :character :expression :dialogue] (if (= (count data) 5)
                                                      (let [dialogue-1 (nth data 3)
                                                            dialogue-2 (nth data 4)
                                                            dialogue (str dialogue-1 "," dialogue-2)]
                                                        (conj (into [] (drop-last 2 data)) dialogue))
                                                      data))))

(defn make-content-maps [data]
  (mapv make-content-map data))

(defn read-data [file]
  (->
   (get-data file)
   (split-data)
   (make-content-maps)))

(def read-all-data (comp (partial into []) flatten (partial mapv read-data)))

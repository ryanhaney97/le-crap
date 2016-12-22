(ns le-crap.write-data
  (:require [clojure.string :as string]
            [le-crap.read-data :refer [get-data]]
            [le-crap.read-script :refer [read-script]]
            [le-crap.util :refer [spit-jp slurp-jp]]))

(defn get-translations [file]
  (read-script file))

(defn split-data [data]
  (map #(string/split %1 #",") data))

(defn replace-data [data translation]
  (vector (first data) (:character translation) (nth data 2) (:dialogue translation)))

(defn apply-translations [data translations]
  (map replace-data data translations))

(defn- rebuild-commas-sub [data]
  (reduce #(str %1 "," %2) data))

(defn rebuild-commas [data]
  (map rebuild-commas-sub data))

(defn rebuild-data [data]
  (str (reduce #(str %1 "\r\n" %2) data) "\r\n-1,,0,,"))

(defn translate-file [file translations]
  (-> file
      (get-data)
      (split-data)
      (apply-translations translations)
      (rebuild-commas)
      (rebuild-data)))

(defn write-data [file translations]
  (spit-jp file (translate-file file translations)))

(defn script->data [script-file files]
  (if (not (empty? script-file))
    (loop [files files translations (get-translations script-file)]
      (write-data (first files) translations)
      (if (and (not= (count files) 1) (> (count translations) (count (get-data (first files)))))
        (recur (rest files) (drop (count (get-data (first files))) translations))))))

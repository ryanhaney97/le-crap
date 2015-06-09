(ns le-crap.core
  (:require [clojure.string :as string]
            [net.cgrand.enlive-html :as html]))

(def test-url "http://lenen.shoutwiki.com/wiki/Brilliant_pagoda_or_haze_castle/Story/Tutorial")
(def test-file "resources/talk_tutorial_00.txt")

(defn rprintln [content]
  (println content)
  content)

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn get-content [html-resource]
  (map :content (html/select html-resource [:table :tr :p])))

(defn handle-remaining-tag [tag]
  (if (map? tag)
    (let [content (first (:content tag))]
      (if (map? content)
        (recur content)
        content))
    tag))

(defn filter-content [content]
  (->>
   (map #(map handle-remaining-tag %1) content)
   (map #(filter identity %1))
   (map #(filter (fn [x] (not= x "<Put stage description>")) %1))
   (map #(filter (complement empty?) %1))
   (filter (complement empty?))))

(defn reduce-content [content]
  (map #(reduce str %1) content))

(defn remove-japanese [content]
  (let [partitioned (partition 3 content)
        japanese-removed (map #(vector (first %1) (nth %1 2)) partitioned)]
    (flatten japanese-removed)))

(defn make-content-map [content]
  (let [partitioned (partition 2 content)
        content-map (map #(sorted-map :character (first %1) :dialogue (second %1)) partitioned)]
    content-map))

(defn get-translations [url]
  (-> url
      (fetch-url)
      (get-content)
      (filter-content)
      (reduce-content)
      (remove-japanese)
      (make-content-map)))

(defn format-translations [translations]
  (let [replacer (fn [m]
                   {:character (:character m) :dialogue (let [dialogue (:dialogue m)
                                                              commas-replaced (string/replace dialogue "," "")
                                                              result (string/replace commas-replaced "\n" ",")]
                                                          (if (= commas-replaced result)
                                                            (str result ",")
                                                            result))})]
    (map replacer translations)))

(defn get-data [file]
  (butlast (string/split (slurp file) #"\r\n")))

(defn split-data [data]
  (map #(string/split %1 #",") data))

(defn replace-data [data translation]
  (vector (first data) (:character translation) (nth data 2) (:dialogue translation)))

(defn apply-translations [data translations]
  (map replace-data data (format-translations translations)))

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
      (rebuild-data)
      ((partial spit file))))

(let [translations (get-translations test-url)]
  (translate-file test-file translations))

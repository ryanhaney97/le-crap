(ns le-crap.core
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [net.cgrand.enlive-html :as html]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]])
  (:import [java.io FileOutputStream
            OutputStreamWriter])
  (:gen-class))

(defn rprintln [item]
  (println (str (first item)))
  item)

(def base-path (comp io/file (partial str "./text_dat/talk/")))

(def base-url (partial str "http://lenen.shoutwiki.com/wiki/Brilliant_pagoda_or_haze_castle/le-crap/"))

(def cache-url (partial str "http://webcache.googleusercontent.com/search?q=cache:"))

(def paths {(base-url "Tutorial") [(base-path "talk_tutorial_00.txt")
                                   (base-path "talk_tutorial_01.txt")
                                   (base-path "talk_tutorial_02.txt")
                                   (base-path "talk_tutorial_03.txt")]
            (base-url "Shrine_Team_Haze%27s_Scenario") [(base-path "talk_Yabusame_Mitsumo_0.txt")
                                                        (base-path "talk_Yabusame_Mitsumo_1.txt")
                                                        (base-path "talk_Yabusame_Kujiru_0.txt")
                                                        (base-path "talk_Yabusame_Kujiru_1.txt")
                                                        (base-path "talk_Yabusame_Kaisen_0.txt")
                                                        (base-path "talk_Yabusame_Kaisen_1.txt")]
            (base-url "Shrine_Team_Brilliant%27s_Scenario") [(base-path "talk_Yabusame_Souko_0.txt")
                                                             (base-path "talk_Yabusame_Souko_1.txt")
                                                             (base-path "talk_Yabusame_Medias_0.txt")
                                                             (base-path "talk_Yabusame_Medias_1.txt")
                                                             (base-path "talk_Yabusame_Kunimitsu_0.txt")
                                                             (base-path "talk_Yabusame_Kunimitsu_1.txt")]
            (base-url "Shrine_Team_Neutral%27s_Scenario") [(base-path "talk_Yabusame_Kurohebi_0.txt")
                                                           (base-path "talk_Yabusame_Kurohebi_1.txt")
                                                           (base-path "talk_Yabusame_Jun_0.txt")
                                                           (base-path "talk_Yabusame_Jun_1.txt")
                                                           (base-path "talk_Yabusame_Aoji_0.txt")
                                                           (base-path "talk_Yabusame_Aoji_1.txt")
                                                           (base-path "talk_Yabusame_Shou_0.txt")
                                                           (base-path "talk_Yabusame_Shou_1.txt")
                                                           (base-path "talk_Yabusame_Lumen_0.txt")
                                                           (base-path "talk_Yabusame_Lumen_1.txt")
                                                           (base-path "talk_Yabusame_Sese_0.txt")
                                                           (base-path "talk_Yabusame_Sese_1.txt")
                                                           (base-path "talk_Yabusame_Suzumi_0.txt")
                                                           (base-path "talk_Yabusame_Suzumi_1.txt")]})

(defn spit-jp [file string]
  (let [fos (FileOutputStream. file)
        out (OutputStreamWriter. fos "Shift_JIS")]
    (.write out string)
    (.close out)))

(defn fetch-url [url]
  (let [site (go
              (html/html-resource (java.net.URL. url)))
        cancel (timeout 5000)]
    (go
     (let [winner (alts! [site cancel])]
       (if (= (second winner) cancel)
         (do
           (println "Error getting data, attempting to access cached version...")
           (let [another-attempt (go
                                  (html/html-resource (java.net.URL. (str cache-url (string/replace url "http://" "")))))
                 cancel (timeout 5000)
                 winner (alts! [another-attempt cancel])]
             (if (= (second winner) cancel)
               (println "Unable to obtain url! Please check your internet connection.")
               (first winner))))
         (first winner))))))

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
      (<!!)
      (get-content)
      (filter-content)
      (reduce-content)
      (make-content-map)))

(defn format-translations [translations]
  (let [replacer (fn [translation]
                   (let [dialogue (:dialogue translation)
                         commas-replaced (string/replace dialogue "," ",@")
                         result (string/replace commas-replaced "\n" ",")]
                     (if (= commas-replaced result)
                       (assoc translation :dialogue (str result ","))
                       (assoc translation :dialogue result))))]
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
      ((partial spit-jp file))))

(defn translate-files [files url]
  (println (str "Translating:" url))
  (loop [files files translations (get-translations url)]
    (translate-file (first files) translations)
    (if (and (not= (count files) 1) (> (count translations) (count (get-data (first files)))))
      (recur (rest files) (drop (count (get-data (first files))) translations)))))

(defn -main []
  (doall (map #(translate-files (val %1) (key %1)) paths))
  (println "Patching Complete!"))

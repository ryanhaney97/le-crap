(ns le-crap.read-script
  (:require [clojure.string :as string]
            [le-crap.util :refer [slurp-jp]]))

(defn parse-text [text]
  (let [text (string/split text #"((?<!:):(?!:))")
        text (map #(string/replace %1 #"::" ":") text)
        text (map string/trim text)]
    (rest text)))

(defn make-content-map [content]
  (let [partitioned (partition 2 content)
        content-map (map #(sorted-map :character (first %1) :dialogue (second %1)) partitioned)]
    content-map))

(defn format-translations [translations]
  (let [replacer (fn [translation]
                   (let [dialogue (:dialogue translation)
                         commas-replaced (string/replace dialogue "," "@,")
                         result (string/replace commas-replaced #"\n\s*" ",")]
                     (if (= commas-replaced result)
                       (assoc translation :dialogue (str result ","))
                       (assoc translation :dialogue result))))]
    (map replacer translations)))

(def read-script (comp format-translations make-content-map parse-text slurp-jp))

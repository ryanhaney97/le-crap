(ns le-crap.util
  (:import [java.io FileOutputStream
            OutputStreamWriter
            FileInputStream
            InputStreamReader
            BufferedReader]))

(defn spit-jp [file string]
  (let [file (clojure.java.io/file file)]
    (.mkdirs (clojure.java.io/file (.getParent file)))
    (let [fos (FileOutputStream. file)
          out (OutputStreamWriter. fos "UTF-16LE")]
      (.write fos (byte-array [0xFF 0xFE]))
      (.write out string)
      (.close out)
      (.close fos))))

(defn slurp-jp [file]
 (let [fis (FileInputStream. file)
       in (BufferedReader. (InputStreamReader. fis "UTF-16LE"))]
   (loop [text ""]
     (if (not (.ready in))
       (do
         (.close in)
         text)
       (recur (str text (.readLine in) "\n"))))))

(defn update-map-vals
  "Utility function. Given a map and a function, applies that function to all values in the map."
  [func m]
  (into {} (map #(vector (key %1) (func (val %1))) m)))

(defn update-map-keys
  "Utility function. Given a map and a function, applies that function to all keys in the map."
  [func m]
  (into {} (map #(vector (func (key %1)) (val %1)) m)))

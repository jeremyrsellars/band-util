(ns us.sellars.band-util.zip
  (:require [clojure.java.io :as io])
  (:import [java.util.zip ZipEntry ZipOutputStream]
           [java.io File]))

(defmacro ^:private with-entry
  [zip entry-name & body]
  `(let [^ZipOutputStream zip# ~zip]
     (.putNextEntry zip# (ZipEntry. ~entry-name))
     ~@body
     (flush)
     (.closeEntry zip#)))

(defn create-zip
  [zip-filename content-map]
  (assert (string? zip-filename))
  (with-open [output (ZipOutputStream. (io/output-stream zip-filename))]
    (doseq [[out input-f-or-string] content-map]
      (if-let [input-stream (and (instance? File input-f-or-string)
                                 (io/input-stream input-f-or-string))]
        (with-entry output out
          (io/copy input-stream output))
        (with-entry output out
          (println input-f-or-string output))))))

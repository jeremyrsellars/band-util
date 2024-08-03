(ns us.sellars.band-util.file
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [flatland.ordered.map :refer [ordered-map]]))

(def header-defaults
  (ordered-map
   :version 0
   :name "Untitled"
   :source ""
   :song-writer "" 
   :genre "Praise/Worship"
   :key ""
   :bpm 12.34
   :play-time 0
   :time-sig "4/4"))

(def header-ks
  (into [] (keys header-defaults)))

(defn file-header-section
  [bf]
  (let [merged (merge header-defaults bf)]
    (string/join "\n"
                 (concat
                  (cons (string/join " " (cons "[Header] //" (interpose "|" (map symbol (keys header-defaults)))))
                        (map merged header-ks))
                  [nil "[End Header]" nil nil]))))

#_(print (file-header-section {:name "Jeremy was here"}))
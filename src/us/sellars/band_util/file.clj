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

(defn map-filenames->tracks
  [track-pattern-map filenames]
  (reduce
   (fn [m filename]
     (if-let [[section match]
              (some (fn [[section pattern]]
                      (when-let [match (re-find pattern filename)]
                        [section (cond-> match (not (string? match)) first)])) ; just in case the pattern has matching groups and thus returns [match g1 g2...]
                    track-pattern-map)]
       (update m section (fn [tracks] (conj (or tracks []) [match filename])))
       m))
   (empty track-pattern-map)
   filenames))

(defn file-tracks-section
  [tracks]
  (->> (-> ["[Tracks]"]
           (into (mapcat (fn [[section tracks]](reduce into [(str "[" section "]")] tracks))) tracks)
           (conj "[End Tracks]" nil nil))
    (string/join "\n")))

(comment
  (def ex-track-pattern-map
    (ordered-map
     "Percussion" #"Drums|Perc"
     "Melodic" #"Bass|EG1|EG2|Piano|Synth|Pads|VXFX|Syn(LD)"))

  (def ex-file-listing
    (re-seq #"\S+"
            "Drums.ogg
Perc.ogg
Bass.ogg
EG1.ogg
EG2.ogg
Piano.ogg
Synth.ogg
Pads.ogg
VXFX.ogg
SynLD.ogg"))

  (map-filenames->tracks ex-track-pattern-map ex-file-listing)
  (print (file-tracks-section (map-filenames->tracks ex-track-pattern-map ex-file-listing)))
  :-)

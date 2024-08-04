(ns us.sellars.band-util.file
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [flatland.ordered.map :refer [ordered-map]]
            [us.sellars.band-util.zip :as bzip])
  (:import [java.io File]))

(def ^:dynamic project-dir (io/file "."))

(defn simple-section
  ([optional? header](simple-section optional? header nil))
  ([optional? header body]
   (when (or (seq body) (not optional?))
     (str "[" header "]\n" body (when body "\n") "[End " header "]\n\n"))))

;; [Header]
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

;; [Tracks]

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

;; [Sections]
(defn file-sections-section
  [sections]
  (when (seq sections)
    (-> ["[Sections]"]
        (into (map str) (cond-> sections (string? sections) vector))
        (conj "[End Sections]" nil nil)
        (as-> ss (string/join "\n" ss)))))

(comment
  (def ex-sections
    '[Ct 8 Intro 32])
  (println (file-sections-section ex-sections))
  :-)

;; [Presets]
(def ^{:arglists '[[body]]}
  file-presets-section
  (partial simple-section :optional "Presets"))

;; [Chords]
(def ^{:arglists '[[body]]}
  file-chords-section
  (partial simple-section :optional "Chords"))

;; [Info]
(def ^{:arglists '[[body]]}
  file-info-section
  (partial simple-section :optional "Info"))

(defn tracks-and-file-content
  [{:keys [tracks track-patterns filenames sections presets chords info]
    :or {track-patterns {"Audio" #"[^.]+"}}
    :as bf}]
  (let [tracks2 (or tracks (map-filenames->tracks track-patterns filenames))]
    {:tracks              tracks2
     :tracks.text/content (str
                           (file-header-section bf)
                           (file-tracks-section tracks2)
                           (file-sections-section sections)
                           (file-presets-section presets)
                           (file-chords-section chords)
                           (file-info-section info))}))

(defn- fixup-file
  [f]
  (or (and (instance? File f) f)
      (when-let [^File f (and (string? f) (re-find #"^\S+$" f) (io/file f))]
        (if (.isAbsolute f)
          f
          (io/file project-dir (str f))))))

(defn- slurp-file-or-nil
  [s-or-f]
  (when-let [^File f (or (and (string? s-or-f) (re-find #"^\S+$" s-or-f) (fixup-file s-or-f))
                         (and (instance? File s-or-f) s-or-f))]
    (when (.exists f) (slurp f))))

(defn- slurp-file-or-string
  [s-or-f]
  (or (slurp-file-or-nil s-or-f)
      s-or-f))

(defn- slurp-file-or-data
  [data-or-f]
  (if-let [content (slurp-file-or-nil data-or-f)]
    (read-string content) ; to-do: consider using clojure.tools.reader(.edn)
    data-or-f))

(defn create-tracks
  [{:keys [dir project-file out-file audio-file?]
    :or {dir          (or (System/getenv "BAND_DIR") ".")
         project-file (or (System/getenv "BAND_PROJECT_FILE") "tracks.edn")
         out-file     "Tracks.txt"
         audio-file?  #(some->> % str (re-find #"(?i).(wav|mp3|ogg)$") boolean)}}]
  (binding [project-dir (io/as-file dir)]
    (let [bf (slurp-file-or-data project-file)
          bf (-> (if (string? bf) nil bf)
                 (update :filenames
                         #(or % (->> (file-seq project-dir)
                                     (sequence (comp (filter audio-file?) (map (fn [^File f](.getName f))))))))
                 (update :name #(or % (.getName project-dir)))
                 (update :sections slurp-file-or-string)
                 (update :presets slurp-file-or-string)
                 (update :chords slurp-file-or-string)
                 (update :info slurp-file-or-string))]
      (tracks-and-file-content bf))))

(defn create-tracks-file
  [{:keys [dir out-file]
    :or   {out-file "Tracks.txt"}
    :as   opts}]
  (binding [project-dir (io/as-file dir)]
    (let [{:keys [tracks] tracks-txt-content :tracks.text/content} (create-tracks opts)]
      (spit (fixup-file out-file) tracks-txt-content :encoding "utf8")
      (println "================================================")
      (println tracks-txt-content)
      (println "================================================")
      tracks)))

(defn create-tracks-zip
  [{:keys [dir out-zip]
    :or {dir     (or (System/getenv "BAND_DIR") ".")
         out-zip (str (.getName (io/as-file dir)) ".zip")}
    :as opts}]
  (binding [project-dir (io/as-file dir)]
    (let [{:keys [tracks] tracks-txt-content :tracks.text/content} (create-tracks opts)
          zip-file (fixup-file out-zip)]
      (println "================================================")
      (println tracks-txt-content)
      (println "================================================")
      (bzip/create-zip (str zip-file)
                       {}))))

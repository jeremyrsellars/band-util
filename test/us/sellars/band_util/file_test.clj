(ns us.sellars.band-util.file-test
  (:require ;[clojure.java.io :as io]
            ;[clojure.string :as string]
            [clojure.test :as test :refer [deftest testing is]]
            [flatland.ordered.map :refer [ordered-map]]
            [us.sellars.band-util.file :as bf]))

; "[Header] // #version | Name | Source | Songwriter | Genre | BPM | TimeSig | PlayTime"

(deftest header
  (is (= "[Header] // version | name | source | song-writer | genre | key | bpm | play-time | time-sig
0
Better is One Day
Guide Tracks
Matt Redman
Praise/Worship
Bm
80.00
0:00
4/4

[End Header]

" 
         (bf/file-header-section {:name "Better is One Day"
                                  :version 0
                                  :source "Guide Tracks"
                                  :genre "Praise/Worship"
                                  :time-sig "4/4"
                                  :key "Bm"
                                  :bpm "80.00"
                                  :song-writer "Matt Redman"
                                  :play-time "0:00"}))))


(deftest tracks-header
  (let [filenames
        ["_g1_drum_asdf.mp3" "_keys_asdf.mp3" "_bass_asdf.mp3" "_gtr1_asdf.mp3" "_gtr2_asdf.mp3" "_gtr3_asdf.mp3" "_g5_inst_asdf.mp3" "_ldvox_asdf.mp3" "_vox1_asdf.mp3" "_vox2_asdf.mp3" "_vox3_asdf.mp3" "_vox4_asdf.mp3" "_voxgtr_asdf.mp3" "_g3_vox_asdf.mp3" "_choir3_asdf.mp3" "_g7_talk.mp3"]
        track-pattern-map (ordered-map
                           "Percussion" #"drum"
                           "Melodic" #"keys?\d?|bass|gtr\d?|g\d?_inst"
                           "Vocals" #"(ld)vox\d?"
                           "Talk" #"choir\d?|talk")]
    (is (= {"Percussion" [["drum" "_g1_drum_asdf.mp3"]],
            "Melodic"
            [["keys" "_keys_asdf.mp3"]
             ["bass" "_bass_asdf.mp3"]
             ["gtr1" "_gtr1_asdf.mp3"]
             ["gtr2" "_gtr2_asdf.mp3"]
             ["gtr3" "_gtr3_asdf.mp3"]
             ["g5_inst" "_g5_inst_asdf.mp3"]
             ["gtr" "_voxgtr_asdf.mp3"]],
            "Vocals" [["ldvox" "_ldvox_asdf.mp3"]],
            "Talk" [["choir3" "_choir3_asdf.mp3"] ["talk" "_g7_talk.mp3"]]}
           (bf/map-filenames->tracks track-pattern-map filenames)))
    (is (= "[Tracks]
[Percussion]
drum
_g1_drum_asdf.mp3
[Melodic]
keys
_keys_asdf.mp3
bass
_bass_asdf.mp3
gtr1
_gtr1_asdf.mp3
gtr2
_gtr2_asdf.mp3
gtr3
_gtr3_asdf.mp3
g5_inst
_g5_inst_asdf.mp3
gtr
_voxgtr_asdf.mp3
[Vocals]
ldvox
_ldvox_asdf.mp3
[Talk]
choir3
_choir3_asdf.mp3
talk
_g7_talk.mp3
[End Tracks]

"
           (bf/file-tracks-section (bf/map-filenames->tracks track-pattern-map filenames))))))

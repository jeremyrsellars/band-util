# Utility functions for the WorshipSong Band app

## EDN data -> tracks.txt

```clojure
(def track-pattern-map
  (ordered-map
   "Percussion" #"drum"
   "Melodic" #"keys?\d?|bass|gtr\d?|g\d?_inst"
   "Vocals" #"(ld)vox\d?"
   "Talk" #"choir\d?|talk"))
(as-> ["_g1_drum_asdf.mp3" "_keys_asdf.mp3" "_bass_asdf.mp3" "_gtr1_asdf.mp3" "_gtr2_asdf.mp3" "_gtr3_asdf.mp3" "_g5_inst_asdf.mp3" "_ldvox_asdf.mp3" "_vox1_asdf.mp3" "_vox2_asdf.mp3" "_vox3_asdf.mp3" "_vox4_asdf.mp3" "_voxgtr_asdf.mp3" "_g3_vox_asdf.mp3" "_choir3_asdf.mp3" "_g7_talk.mp3"]
      tracks
     (map-filenames->tracks track-pattern-map tracks)
     (do ;(clojure.pprint/pprint tracks)
         (println (file-tracks-section tracks))
         tracks))

=>
[Tracks]
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


{"Percussion" [["drum" "_g1_drum_asdf.mp3"]],
 "Melodic"
 [["keys" "_keys_asdf.mp3"]
  ["bass" "_bass_asdf.mp3"]
  ["gtr1" "_gtr1_asdf.mp3"]
  ["gtr2" "_gtr2_asdf.mp3"]
  ["gtr3" "_gtr3_asdf.mp3"]
  ["g5_inst" "_g5_inst_asdf.mp3"]
  ["gtr" "_voxgtr_asdf.mp3"]],
 "Vocals" [[["ldvox" "ld"] "_ldvox_asdf.mp3"]],
 "Talk" [["choir3" "_choir3_asdf.mp3"] ["talk" "_g7_talk.mp3"]]}

```

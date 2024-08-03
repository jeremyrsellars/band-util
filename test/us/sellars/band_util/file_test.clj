(ns us.sellars.band-util.file-test
  (:require ;[clojure.java.io :as io]
            ;[clojure.string :as string]
            [clojure.test :as test :refer [deftest testing is]]
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

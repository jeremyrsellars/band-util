(ns user
  (:require [hashp.core]))

(doto 'us.sellars.band-util.file
  (println "-- requiring the namespace")
  (require)
  (in-ns))

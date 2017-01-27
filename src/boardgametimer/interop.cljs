(ns boardgametimer.interop
  (:require [cljs-time.core :as time]))

(defn now []
  (time/now))

(defn interval-in-millis [t1 t2]
  (time/in-millis (time/interval t1 t2)))
(ns boardgametimer.interop
  (:require [clj-time.core :as time]))

(defn now []
  (time/now))

(defn interval-in-millis [t1 t2]
  (time/in-millis (time/interval t1 t2)))

(defn to-time [ms]
  (let [sec (quot ms 1000)
        min (quot sec 60)
        hr (quot min 60)]
    (format "%d:%02d:%02d" hr (mod min 60) (mod sec 60))))


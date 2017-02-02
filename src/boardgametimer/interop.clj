(ns boardgametimer.interop
  (:require [clj-time.core :as time]
            [clj-time.coerce :as c]
            [clj-time.format :as f]))

(defn now []
  (time/now))

(defn interval-in-millis [t1 t2]
  (time/in-millis (time/interval t1 t2)))

(defn to-time [ms]
  (let [sec (quot ms 1000)
        min (quot sec 60)
        hr (quot min 60)]
    (format "%d:%02d:%02d" hr (mod min 60) (mod sec 60))))

(defn format [ms]
  (if (>= ms 0)
    (f/unparse (f/formatter "m:ss") (c/from-long ms))
    (f/unparse (f/formatter "-m:ss") (c/from-long (- ms)))))

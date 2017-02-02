(ns boardgametimer.interop
  (:require [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            [cljs-time.format :as f]))

(defn now []
  (t/now))

(defn interval-in-millis [t1 t2]
  (t/in-millis (t/interval t1 t2)))

(defn format [ms]
  (if (>= ms 0)
    (f/unparse (f/formatter "m:ss") (c/from-long ms))
    (f/unparse (f/formatter "-m:ss") (c/from-long (- ms)))))

(ns boardgametimer.common
  (:require [boardgametimer.interop :as i]))

(defonce my-game (atom {}))

(defn single [coll]
  (assert (<= (count coll) 1))
  (first coll))

(defn map-vals
  "Maps a function over the values of an associative collection."
  [f coll]
  (into {} (for [[k v] coll] [k (f v)])))

(defn create-game [ms-per-player]
  {:game/players {}
   :game/round   0
   :game/ms-per-player ms-per-player})

(defn create-player [name]
  {:player/name     name
   :player/ms-spent 0})

(defn add-player [{:keys [game/players] :as game} {:keys [player/name] :as player}]
  (let [index (count players)]
    (assoc-in game [:game/players name] (assoc player :player/index index))))

(defn set-first-player [game name]
  (assoc game :game/first-player name))

(defn in-round? [{:keys [game/start-time game/current-player]}]
  (and start-time current-player))

(defn out-of-round? [{:keys [game/start-time game/current-player]}]
  (nil? current-player))

(defn paused? [{:keys [game/start-time game/current-player]}]
  (and (nil? start-time) current-player))

(defn- start-player [game name]
  (assoc game :game/current-player name
              :game/start-time (i/now)))

(defn- stop-player [{:keys [game/players game/current-player game/start-time] :as game}]
  (let [{:keys [player/ms-spent]} (get players current-player)]
    (-> game
        (assoc-in [:game/players current-player :player/ms-spent] (+ ms-spent (i/interval-in-millis start-time (i/now)))))))

(defn start-round [{:keys [game/first-player] :as game}]
  (if (out-of-round? game)
    (-> game
        (update-in [:game/round] inc)
        (update-in [:game/players] (partial map-vals (fn [player] (assoc player :player/active? true))))
        (start-player first-player))
    game))

(defn- get-next-player [{:keys [game/players game/current-player]} & [name]]
  (let [active-players (->> players vals (filter :player/active?) (sort-by :player/index))
        current-index (->> (get players (or name current-player)) :player/index)]
    (-> (filter (fn [{:keys [player/index]}] (> index current-index)) active-players)
        (concat active-players)
        first)))

(defn player-action-done [game]
  (if (in-round? game)
    (-> game
        stop-player
        (start-player (:player/name (get-next-player game))))
    game))

(defn player-action-pass [{:keys [game/players game/current-player] :as game}]
  (if (in-round? game)
    (let [first-pass? (->> players
                           vals
                           (every? :player/active?))
          game' (-> game
                    stop-player
                    (assoc-in [:game/players current-player :player/active?] false))]
      (cond-> game'
              first-pass? (assoc :game/first-player current-player)
              true (start-player (:player/name (get-next-player game')))))
    game))

(defn toggle-pause [{:keys [game/current-player game/start-time] :as game}]
  (cond
    (in-round? game) (-> game
                         stop-player
                         (dissoc :game/start-time))
    (paused? game) (-> game
                       (start-player current-player))
    :else game))

(comment

  (reset! my-game
          (-> (create-game)
              (add-player (create-player "Frederik"))
              (add-player (create-player "Kasper"))
              (add-player (create-player "Jonas"))
              (set-first-player "Kasper")))

  (swap! my-game start-round)

  (swap! my-game player-action-done)
  (swap! my-game player-action-pass)

  (swap! my-game toggle-pause)

  )



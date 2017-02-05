(ns boardgametimer.core
  (:require [boardgametimer.common :as c]
            [boardgametimer.interop :as i]
            [reagent.core :as r]))

(enable-console-print!)

(defonce app-state (r/atom {:ui-state {}
                            :game     {}}))

(defn set-current-spent [{:keys [game/start-time] :as game}]
  (let [current-spent (when (c/in-round? game)
                        (i/interval-in-millis start-time (i/now)))]
    (assoc game :game/current-spent current-spent)))

(defn tick! []
  (swap! app-state update :game set-current-spent)
  (let [{:keys [game/current-spent game/current-player game/players] :as game} (:game @app-state)
        ms-spent (+ current-spent (get-in players [current-player :player/ms-spent]))]
    (when (c/in-round? game) (js/setTimeout tick! (- 1000 (mod (- ms-spent 1) 1000))))))

(defn create-game-view []
  [:div
   [:div "Minutter per spiller"
    [:input {:on-change #(swap! app-state assoc-in [:ui-state :ms-per-player] (-> % .-target .-value))
             :type      :text
             :value     (get-in @app-state [:ui-state :ms-per-player])}]]
   [:div
    (let [ms-per-player (->> (get-in @app-state [:ui-state :ms-per-player])
                             (js/parseInt)
                             (* 60 1000))]
      [:button {:disabled (not (re-matches #"\W*\d+\W*" (or (get-in @app-state [:ui-state :ms-per-player]) "")))
                :on-click #(swap! app-state assoc :game (c/create-game ms-per-player))}
       "Nyt spil"])]])

(defn players-view [{:keys [game/first-player game/current-player game/ms-per-player game/players game/current-spent] :as game}]
  (->> players
       vals
       (sort-by :player/index)
       (mapv (fn [{:keys [player/name player/ms-spent player/active?]}]
               (let [current-player? (= name current-player)]
                 [:div
                  (when (nil? first-player)
                    [:button {:on-click #(swap! app-state update :game c/set-first-player name)}
                     "Første spiller"])
                  (str name " " (i/format (- ms-per-player ms-spent (when current-player? current-spent))) " " active?)
                  (when current-player?
                    [:button {:disabled (not (c/in-round? game))
                              :on-click #(swap! app-state update :game (comp set-current-spent c/player-action-done))}
                     "Næste"])
                  (when current-player?
                    [:button {:disabled (not (c/in-round? game))
                              :on-click #(swap! app-state update :game (comp set-current-spent c/player-action-pass))}
                     "Pas"])])))
       (concat [])))

(defn add-players-view [{:keys [game/first-player] :as game}]
  [:div
   [:div
    [:input {:on-change #(swap! app-state assoc-in [:ui-state :player] (-> % .-target .-value))
             :type      :text
             :value     (get-in @app-state [:ui-state :player])}]
    [:button {:on-click #(swap! app-state update :game c/add-player (c/create-player (get-in @app-state [:ui-state :player])))}
     "Tilføj spiller"]]

   [:div {:style {:color :black}}
    (players-view game)]

   [:div
    [:button {:disabled (not (and first-player (c/out-of-round? game)))
              :on-click (fn [] (swap! app-state update :game c/start-round) (tick!))}
     "Start spil"]]])

(defn in-game-view [{:keys [game/first-player] :as game}]
  [:div
   [:div {:style {:color :black}}
    (players-view game)]

   [:div
    [:button {:disabled (c/out-of-round? game)
              :on-click (fn [] (swap! app-state update :game c/toggle-pause) (tick!))}
     (if (c/paused? game) "Play" "Pause")]

    [:button {:disabled (not (c/out-of-round? game))
              :on-click (fn [] (swap! app-state update :game c/start-round) (tick!))}
     "Næste runde"]]

   #_[:div {:style {:color :red}} game]])

(defn my-view []
  (let [{:keys [game/round] :as game} (:game @app-state)]
    (cond (empty? game) (create-game-view)
          (= 0 round) (add-players-view game)
          :else (in-game-view game))))

(r/render-component [my-view] (js/document.getElementById "app"))


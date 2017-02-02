(ns boardgametimer.core
  (:require [boardgametimer.common :as c]
            [boardgametimer.interop :as i]
            [reagent.core :as r]))

(enable-console-print!)

(defonce app-state (r/atom {:ui-state {}
                            :game     {}}))

(defn my-view
  []
  (let [{:keys [game/first-player game/current-player game/ms-per-player] :as game} (:game @app-state)]
    [:div
     [:div
      [:button {:on-click #(swap! app-state assoc :game (c/create-game (* 30 60 1000)))}
       "Nyt spil"]]

     [:div
      [:input {:on-change #(swap! app-state assoc-in [:ui-state :text] (-> % .-target .-value))
               :type      :text
               :value     (get-in @app-state [:ui-state :text])}]
      [:button {:on-click #(swap! app-state update :game c/add-player (c/create-player (get-in @app-state [:ui-state :text])))}
       "Tilføj spiller"]
      ]

     [:div
      [:button {:disabled (or (nil? first-player) (not (c/out-of-round? game)))
                :on-click #(swap! app-state update :game c/start-round)}
       "Start runde"]]

     [:div
      [:button {:disabled (c/out-of-round? game)
                :on-click #(swap! app-state update :game c/toggle-pause)}
       (if (c/paused? game) "Play" "Pause")]]

     [:div {:style {:color :black}}
      (->> (get-in game [:game/players])
           vals
           (sort-by :player/index)
           (mapv (fn [{:keys [player/name player/ms-spent player/active?]}]
                   [:div
                    (when (nil? first-player)
                      [:button {:on-click #(swap! app-state update :game c/set-first-player name)}
                       "Første spiller"])
                    (str name " " (i/format (- ms-per-player ms-spent)) " " active?)
                    (when (= name current-player)
                      [:button {:disabled (not (c/in-round? game))
                                :on-click #(swap! app-state update :game c/player-action-done)}
                       "Næste"])
                    (when (= name current-player)
                      [:button {:disabled (not (c/in-round? game))
                                :on-click #(swap! app-state update :game c/player-action-pass)}
                       "Pas"])]))
           (concat []))]]))

(r/render-component [my-view] (js/document.getElementById "app"))


(ns boardgametimer.core
  (:require [boardgametimer.common :as c]
            [reagent.core :as r]))

(enable-console-print!)

(println "This text is printed from src/boardgametimer/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce my-game (r/atom {}))
(defonce app-state (r/atom {}))

(defn my-view
  []
  [:div
   [:div {:style {:color :red}}
    @my-game]
   [:input {:on-change #(swap! app-state assoc :text (-> % .-target .-value))
            :type :text
            :value (:text @app-state)}]
   [:button {:on-click #(swap! my-game c/add-player (c/create-player (:text @app-state)))}
    "Tilføj spiller"]
   [:button {:on-click #(reset! my-game (c/create-game))}
    "Nyt spil"]])

(r/render-component [my-view] (js/document.getElementById "app"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

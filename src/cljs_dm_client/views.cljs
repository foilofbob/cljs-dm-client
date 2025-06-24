(ns cljs-dm-client.views
  (:require
   [re-frame.core :refer [dispatch subscribe] :as re-frame]
   [cljs-dm-client.routes :as routes]
   [cljs-dm-client.campaign-select.views :refer [campaign-select]]
   [cljs-dm-client.timeline.views :refer [timeline]]
   [cljs-dm-client.party.views :refer [party]]
   [cljs-dm-client.locations.views :refer [locations]]
   [cljs-dm-client.xp-tracker.views :refer [xp-tracker]]))

(defmethod routes/panels :campaign-select-panel [] [campaign-select])
(defmethod routes/panels :locations-panel [] [locations])
(defmethod routes/panels :party-panel [] [party])
(defmethod routes/panels :timeline-panel [] [timeline])
(defmethod routes/panels :xp-tracker-panel [] [xp-tracker])

(defn main-panel []
  (let [active-panel @(subscribe [:active-panel])]
    (routes/panels active-panel)))

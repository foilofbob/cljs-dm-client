(ns cljs-dm-client.views
  (:require
   [re-frame.core :refer [dispatch subscribe] :as re-frame]
   [cljs-dm-client.routes :as routes]
   [cljs-dm-client.campaign-select.views :refer [campaign-select]]
   [cljs-dm-client.timeline.views :refer [timeline]]
   [cljs-dm-client.party.views :refer [party]]
   [cljs-dm-client.locations.views :refer [locations]]))

(defmethod routes/panels :campaign-select-panel [] [campaign-select])
(defmethod routes/panels :locations-panel [] [locations])
(defmethod routes/panels :party-panel [] [party])
(defmethod routes/panels :timeline-panel [] [timeline])

(defn main-panel []
  (let [active-panel @(subscribe [:active-panel])]
    (routes/panels active-panel)))

(ns cljs-dm-client.locations.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [cljs-dm-client.layout.views :refer [campaign-panel]]))

(defn locations []
      [campaign-panel
       [:p "LOCATIONS"]])


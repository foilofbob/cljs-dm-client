(ns cljs-dm-client.timeline.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [cljs-dm-client.layout.views :refer [campaign-panel]]))

(defn timeline []
      [campaign-panel
       [:p "TIMELINE"]])


(ns cljs-dm-client.party.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [cljs-dm-client.layout.views :refer [campaign-panel]]))

(defn party []
      [campaign-panel
       [:p "PARTY"]])


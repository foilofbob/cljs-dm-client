(ns cljs-dm-client.layout.subs
  (:require
    [re-frame.core :refer [reg-sub]]))

(reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))

(reg-sub
 :selected-campaign
 (fn [db _]
   (:selected-campaign db)))

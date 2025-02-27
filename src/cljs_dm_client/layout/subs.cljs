(ns cljs-dm-client.layout.subs
  (:require
   [re-frame.core :refer [reg-sub]]
   [clojure.string :as str]))

(reg-sub
  :active-panel
  (fn [db _]
    (:active-panel db)))

(reg-sub
  :active-panel-key
  :<- [:active-panel]
  (fn [panel]
    (->> (-> panel name (str/split "-") drop-last)
      (str/join "-")
      keyword)))

(reg-sub
  :page-error
  (fn [db _]
    (str "Error: " (-> db :page-data :status-text) " (" (-> db :page-data :status) ")")))

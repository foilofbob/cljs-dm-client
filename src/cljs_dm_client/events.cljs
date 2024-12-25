(ns cljs-dm-client.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx]]
   [cljs-dm-client.db :as db]))

(reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-fx
 ::set-active-panel
 (fn [{:keys [db]} [_ active-panel]]
   {:db (assoc db :active-panel active-panel)}))

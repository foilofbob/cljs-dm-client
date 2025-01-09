(ns cljs-dm-client.navigation.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 ::navigate
 (fn [{db :db} [_ handler]]
   {:navigate handler
    :db (-> db
            (assoc :page-data {})
            (assoc :loading-status :loading))}))

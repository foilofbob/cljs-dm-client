(ns cljs-dm-client.navigation.events
  (:require
    [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
 ::navigate
 (fn [{db :db} [_ target]]
   {:navigate target
    :db (-> db
            (assoc :page-data {})
            (assoc :loading-status :loading))
    :fx [[:dispatch [(keyword (str (name target) "-page-load"))]]]}))

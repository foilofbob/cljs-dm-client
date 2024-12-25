(ns cljs-dm-client.campaign-select.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-db
  ::select-campaign
  (fn [db [_ campaign]]
      (assoc db :selected-campaign campaign)))

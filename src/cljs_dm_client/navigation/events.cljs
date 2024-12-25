(ns cljs-dm-client.navigation.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 ::navigate
 (fn [_ [_ handler]]
   {:navigate handler}))

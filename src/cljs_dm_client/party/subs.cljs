(ns cljs-dm-client.party.subs
  (:require
    [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::players
 (fn [db]
     (-> db :page-data :players (or []))))

(reg-sub
 ::items
 (fn [db]
     (-> db :page-data :items (or []))))

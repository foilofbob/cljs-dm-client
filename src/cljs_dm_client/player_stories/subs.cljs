(ns cljs-dm-client.player-stories.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::players
 (fn [db]
     (sort-by :name (-> db :page-data :players (or [])))))

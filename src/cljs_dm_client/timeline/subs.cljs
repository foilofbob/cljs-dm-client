(ns cljs-dm-client.timeline.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::game-days
 (fn [db]
   (->> db :page-data :game-days (sort-by :in-game-day) reverse)))

(reg-sub
 ::campaign-setting
 (fn [db]
     (or (:campaign-setting db)
         [])))

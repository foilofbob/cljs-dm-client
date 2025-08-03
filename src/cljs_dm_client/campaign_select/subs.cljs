(ns cljs-dm-client.campaign-select.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::campaigns
 (fn [db _]
   (-> db :page-data :campaigns)))

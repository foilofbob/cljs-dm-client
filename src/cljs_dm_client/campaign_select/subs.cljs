(ns cljs-dm-client.campaign-select.subs
  (:require
   [cljs-dm-client.components.forms :refer [build-options-from-list]]
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::campaigns
 (fn [db _]
   (-> db :page-data :campaigns)))

(reg-sub
 ::campaign-settings
 (fn [db _]
     (or (-> db :page-data :campaign-settings)
         [])))

(reg-sub
 ::campaign-settings-as-select-options
 :<- [::campaign-settings]
 build-options-from-list)

(ns cljs-dm-client.navigation.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :loading-status
 (fn [db _]
   (or (:loading-status db) :loading)))

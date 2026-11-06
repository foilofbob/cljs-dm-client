(ns cljs-dm-client.logout.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::user
 (fn [db]
   (some-> db :auth :user)))

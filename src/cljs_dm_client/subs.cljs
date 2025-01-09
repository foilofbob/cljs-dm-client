(ns cljs-dm-client.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
  :selected-campaign
  (fn [db _]
    (:selected-campaign db)))

;; Global since notes will be frequently used across many pages
;; TODO: worth scoping to a utils?
(reg-sub
  :notes
  (fn [db _]
    (some-> db :page-data :notes)))


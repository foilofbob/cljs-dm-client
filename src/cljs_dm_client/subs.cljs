(ns cljs-dm-client.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :selected-campaign
 (fn [db _]
     (:selected-campaign db)))

(reg-sub
 :campaign-setting
 (fn [db _]
     (:campaign-setting db)))

;; Global since notes will be frequently used across many pages
;; TODO: worth scoping to a utils?
;; TODO: Scope further versions by Reference / Category?
(reg-sub
  :notes
  (fn [db _]
    (some-> db :page-data :notes)))


(reg-sub
 :notes-for-ref
 (fn [db [_ type id]]
     (some->> db :page-data :notes
              (filter #(and (= type (:reference-type %))
                            (= id (:reference-id %)))))))

(reg-sub
  :modal-open?
  (fn [db [_ modal-key]]
    (some-> db :page-data :modal modal-key :is-open)))

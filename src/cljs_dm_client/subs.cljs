(ns cljs-dm-client.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :page-data
 (fn [db _]
   (:page-data db)))

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
(reg-sub
 :notes
 :<- [:page-data]
 (fn [page-data _]
   (:notes page-data)))

(reg-sub
 :notes-by-category
 :<- [:notes]
 (fn [notes category _]
   (filter #(= category (-> % :category :string)) notes)))

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

(reg-sub
 :toggle-toggled?
 (fn [db [_ toggle-id]]
   (-> db :page-data :toggles (contains? toggle-id))))

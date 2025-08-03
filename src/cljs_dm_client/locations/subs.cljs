(ns cljs-dm-client.locations.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::locations
 (fn [db]
   (sort-by :name (-> db :page-data :locations (or [])))))

(reg-sub
 ::sublocations
 (fn [db]
   (sort-by :name (-> db :page-data :sublocations (or [])))))

(reg-sub
 ::points-of-interest
 (fn [db [_ sublocation-id]]
   (some->> db :page-data :points-of-interest
            (filter #(= (:sublocation-id %) sublocation-id))
            (sort-by :name))))

(reg-sub
 ::selected-location-id
 (fn [db]
   (-> db :page-data :selected-location-id)))

(reg-sub
 ::selected-location
 :<- [::locations]
 :<- [::selected-location-id]
 (fn [[locations selected-location-id]]
   (some->> locations
            (filter #(= selected-location-id (:id %)))
            first)))

(reg-sub
 ::sublocations-to-show
 :<- [::sublocations]
 :<- [::selected-location-id]
 (fn [[sublocations selected-location-id]]
   (filter #(= selected-location-id (:location-id %)) sublocations)))

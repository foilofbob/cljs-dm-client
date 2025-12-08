(ns cljs-dm-client.miscellaneous.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::categories
 (fn [db]
   (sort-by :name (-> db :page-data :categories (or [])))))

(reg-sub
 ::selected-category-id
 (fn [db]
   (-> db :page-data :selected-category-id)))

(reg-sub
 ::selected-category
 :<- [::categories]
 :<- [::selected-category-id]
 (fn [[categories selected-category-id]]
   (some->> categories
            (filter #(= selected-category-id (:id %)))
            first)))

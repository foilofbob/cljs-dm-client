(ns cljs-dm-client.components.components
  (:require
   [re-frame.core :refer [reg-event-db reg-sub]]))

(reg-event-db
 :set-edit-object
 (fn [db [_ path object]]
     (assoc-in db [:page-data path] object)))

(reg-event-db
 :update-edit-field
 (fn [db [_ path field value]]
     (assoc-in db [:page-data path field] value)))

(reg-sub
 :edit-object
 (fn [db [_ path]]
     (-> db :page-data path)))

(defn logical-division [text]
      [:div.logical-division
       [:hr.section-divider.top]
       [:h3 text]
       [:hr.section-divider.bottom]])

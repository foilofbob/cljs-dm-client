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

(defn logical-division [{:keys [text left right class]}]
      [:div.logical-division (when class {:class class})
       [:hr.section-divider.top]
       [:div
        (when left [:div.left left])
        [:h3 text]
        (when right [:div.right right])]
       [:hr.section-divider.bottom]])

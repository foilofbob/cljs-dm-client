(ns cljs-dm-client.components.components
  (:require
   [reagent.core :as r]
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

(def accent-colors
  ["pastel-purple"
   "pastel-yellow"
   "pastel-teal"
   "pastel-red"
   "pastel-blue"
   "pastel-green"])

(defn idx->accent-class [idx]
  (get accent-colors (mod idx (count accent-colors))))

(defn toggle-container [{:keys [title desc header-class edit-fn]} content]
  (let [toggle-atom (r/atom false)]
    (fn [{:keys [title desc edit-fn]} content]
      [:div.toggle-container
       [:div.toggle-header {:class header-class}
        [:button.action-button.toggle {:type     :button
                                       :on-click #(do (swap! toggle-atom not) (print (str @toggle-atom)))}
         (if @toggle-atom "-" "+")]
        [:div.toggle-title title]
        [:div.toggle-description desc]
        (when edit-fn
          [:button.edit-button {:type     :button
                                :on-click edit-fn}])]
       (when @toggle-atom content)])))

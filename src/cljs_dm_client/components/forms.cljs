(ns cljs-dm-client.components.forms
  (:require
   [re-frame.core :refer [dispatch]]))

(defn elem-id [obj-type obj-id obj-key]
  (str obj-type "-" (or obj-id "new") "-" (name obj-key)))

(defn text-input-row [label length obj obj-type obj-key]
  (let [input-id (elem-id obj-type (:id obj) obj-key)
        edit-storage (keyword (str "edit-" obj-type))]
    [:div.input-row
     [:label {:for input-id}
      label]
     [:input {:id         input-id
              :value      (obj-key obj)
              :max-length length
              :on-change  #(dispatch [:update-edit-field edit-storage obj-key (-> % .-target .-value)])}]]))

(defn- bound-numeric-input [min max event]
       (let [value (-> event .-target .-value int)]
            (cond
             (< value min) min
             (> value max) max
             :else value)))

(defn number-input-row [{:keys [label min max obj obj-type obj-key]
                         :or {min 0 max 999999}}]
      (let [input-id (elem-id obj-type (:id obj) obj-key)
            edit-storage (keyword (str "edit-" obj-type))]
           [:div.input-row
            [:label {:for input-id}
             label]
            [:input {:id        input-id
                     :type      :number
                     :min       min
                     :max       max
                     :value     (obj-key obj)
                     :on-change #(dispatch [:update-edit-field edit-storage obj-key (bound-numeric-input min max %)])}]]))

(defn textarea-input-row [label length rows obj obj-type obj-key]
  (let [input-id (elem-id obj-type (:id obj) obj-key)
        edit-storage (keyword (str "edit-" obj-type))]
    [:div.input-row
     [:label {:for input-id}
      label]
     [:textarea {:id         input-id
                 :value      (obj-key obj)
                 :max-length length
                 :rows       rows
                 :on-change  #(dispatch [:update-edit-field edit-storage obj-key (-> % .-target .-value)])}]]))

(defn checkbox-input-row [label obj obj-type obj-key]
  (let [input-id (elem-id obj-type (:id obj) obj-key)
        edit-storage (keyword (str "edit-" obj-type))]
    [:div.input-row
     [:label {:for input-id}
      label]
     [:input {:id        input-id
              :type      :checkbox
              :value     (obj-key obj)
              :on-change #(dispatch [:update-edit-field edit-storage obj-key (-> % .-target .-checked)])}]]))

(defn select-input-row [label obj obj-type obj-key options]
  (let [input-id (elem-id obj-type (:id obj) obj-key)
        edit-storage (keyword (str "edit-" obj-type))]
    [:div.input-row
     [:label {:for input-id}
      label]
     (into [:select {:id         input-id
                     :value      (obj-key obj)
                     :on-change  #(dispatch [:update-edit-field edit-storage obj-key (-> % .-target .-value)])}]
           (for [option options]
             [:option {:value (:value option)} (:label option)]))]))

(defn build-options
      "This will take a list of objects and return a list of maps with id->value and name->label"
      [{:keys [objects blank? sort]
        :or {blank? true sort :name}}]
      (concat (if blank? `({:value "" :label ""}) `())
              (->> objects
                   (sort-by sort)
                   (map (fn [obj]
                            {:value (:id obj)
                             :label (:name obj)})))))

(defn build-options-from-list
      [objects]
      (build-options {:objects objects}))

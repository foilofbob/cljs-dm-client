(ns cljs-dm-client.locations.views
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-dm-client.components.components :refer [idx->accent-class
                                                 toggle-container
                                                 logical-division]]
   [cljs-dm-client.components.forms :refer [text-input-row
                                            textarea-input-row]]
   [cljs-dm-client.components.notes :refer [build-notes
                                            note-modal
                                            open-edit-note-modal
                                            standard-note-columns]]
   [cljs-dm-client.layout.views :refer [campaign-panel
                                        loading-wrapper]]
   [cljs-dm-client.locations.events :as events]
   [cljs-dm-client.locations.subs :as subs]
   ["reactstrap/lib/Modal" :default Modal]
   ["reactstrap/lib/ModalBody" :default ModalBody]
   ["reactstrap/lib/ModalFooter" :default ModalFooter]
   ["reactstrap/lib/ModalHeader" :default ModalHeader]))

(def LOCATION_MODAL_KEY :location-modal)
(def SUBLOCATION_MODAL_KEY :sublocation-modal)
(def POI_MODAL_KEY :poi-modal)

(defn location-modal []
  (let [location @(subscribe [:edit-object :edit-location])]
    [:> Modal {:is-open @(subscribe [:modal-open? LOCATION_MODAL_KEY])
               :toggle  #(dispatch [:toggle-modal LOCATION_MODAL_KEY])
               :size    :lg}
     [:> ModalHeader
      (if (:id location)
        "Update Location"
        "Add Location")]
     [:> ModalBody {:class :modal-body}
      [text-input-row "Name" 100 location "location" :name]]
     [:> ModalFooter {:class :modal-footer-buttons}
      [:button.action-link {:on-click #(dispatch [:toggle-modal LOCATION_MODAL_KEY])}
       "Cancel"]
      [:button.action-link {:on-click #(dispatch [::events/delete-location (:id location)])}
       "Delete"]
      [:button.action-button {:on-click #(dispatch [::events/edit-location location])}
       "Save"]]]))

(defn sublocation-modal []
  (let [sublocation @(subscribe [:edit-object :edit-sublocation])]
    [:> Modal {:is-open @(subscribe [:modal-open? SUBLOCATION_MODAL_KEY])
               :toggle  #(dispatch [:toggle-modal SUBLOCATION_MODAL_KEY])
               :size    :lg}
     [:> ModalHeader
      (if (:id sublocation)
        "Update Sublocation"
        "Add Sublocation")]
     [:> ModalBody {:class :modal-body}
      [text-input-row "Name" 100 sublocation "sublocation" :name]
      [textarea-input-row "Description" 5000 15 sublocation "sublocation" :description]]
     [:> ModalFooter {:class :modal-footer-buttons}
      [:button.action-link {:on-click #(dispatch [:toggle-modal SUBLOCATION_MODAL_KEY])}
       "Cancel"]
      [:button.action-link {:on-click #(dispatch [::events/delete-sublocation (:id sublocation)])}
       "Delete"]
      [:button.action-button {:on-click #(dispatch [::events/edit-sublocation sublocation])}
       "Save"]]]))

(defn poi-modal []
  (let [poi @(subscribe [:edit-object :edit-poi])]
    [:> Modal {:is-open @(subscribe [:modal-open? POI_MODAL_KEY])
               :toggle  #(dispatch [:toggle-modal POI_MODAL_KEY])
               :size    :lg}
     [:> ModalHeader
      (if (:id poi)
        "Update Point of Interest"
        "Add Point of Interest")]
     [:> ModalBody {:class :modal-body}
      [text-input-row "Name" 100 poi "poi" :name]]
     [:> ModalFooter {:class :modal-footer-buttons}
      [:button.action-link {:on-click #(dispatch [:toggle-modal POI_MODAL_KEY])}
       "Cancel"]
      [:button.action-link {:on-click #(dispatch [::events/delete-poi (:id poi)])}
       "Delete"]
      [:button.action-button {:on-click #(dispatch [::events/edit-poi poi])}
       "Save"]]]))

(defn location-notes []
  (let [selected-location @(subscribe [::subs/selected-location])
        notes @(subscribe [:notes-for-ref "LOCATION" (:id selected-location)])]
    [standard-note-columns {:notes           notes
                            :container-class :notes-container
                            :selected-obj    selected-location
                            :obj-type        "LOCATION"}]))

(defn poi-details [poi]
  (let [notes @(subscribe [:notes-for-ref "POINT_OF_INTEREST" (:id poi)])]
    [standard-note-columns {:notes           notes
                            :container-class [:notes-container :poi-notes]
                            :selected-obj    poi
                            :obj-type        "POINT_OF_INTEREST"}]))

(defn sublocation-details [sublocation]
  (let [points-of-interest @(subscribe [::subs/points-of-interest (:id sublocation)])]
    [:div.poi-container
     (for [poi points-of-interest]
       [:div.poi {:key (str "poi-" (:id poi))}
        [:div.poi-name
         [:span (:name poi)]
         [:button.edit-button {:type     :button
                               :on-click #(dispatch [::events/open-edit-poi-modal (:id sublocation) poi])}]]
        [poi-details poi]])
     [:button.action-link {:on-click #(dispatch [::events/open-edit-poi-modal (:id sublocation) nil])}
      "Add Point of Interest"]]))

(defn location-details []
  (when-let [selected-location @(subscribe [::subs/selected-location])]
    [:div.locations-page.panel-content
     [:div.panel-content
      [logical-division {:left [:button {:class [:action-button :skinny]
                                         :on-click #(dispatch [::events/open-edit-sublocation-modal nil])}
                                "Add Sublocation"]
                         :text (:name selected-location)
                         :class :no-bottom}]
      [location-notes]

      (let [sublocations @(subscribe [::subs/sublocations-to-show])]
        (into [:div.sublocations]
              (for [[idx sublocation] (map-indexed vector sublocations)]
                [toggle-container {:header-class (idx->accent-class idx)
                                   :title        (:name sublocation)
                                   :desc         (:description sublocation)
                                   :edit-fn      #(dispatch [::events/open-edit-sublocation-modal sublocation])}
                 [sublocation-details sublocation]])))]]))

(defn locations-content []
  (let [locations @(subscribe [::subs/locations])]
    [:<>
     [location-details]
     [:div.right-panel
      [logical-division {:left [:button {:class [:action-button :skinny]
                                         :on-click #(dispatch [::events/open-edit-location-modal nil])}
                                "Add Location"]
                         :text "Locations"}]
      (into [:<>]
            (for [location locations]
              [:button.action-button {:on-click #(dispatch [::events/select-location (:id location)])}
               (:name location)]))]]))

(defn locations []
  [:<>
   [note-modal]
   [location-modal]
   [sublocation-modal]
   [poi-modal]
   [loading-wrapper
    {:container [campaign-panel]
     :content   [locations-content]}]])

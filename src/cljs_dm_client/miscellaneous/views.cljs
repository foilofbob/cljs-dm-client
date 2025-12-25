(ns cljs-dm-client.miscellaneous.views
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-dm-client.components.components :refer [logical-division]]
   [cljs-dm-client.components.forms :refer [text-input-row]]
   [cljs-dm-client.components.notes :refer [build-notes
                                            note-modal
                                            open-edit-note-modal
                                            standard-note-columns]]
   [cljs-dm-client.layout.views :refer [campaign-panel
                                        loading-wrapper]]
   [cljs-dm-client.miscellaneous.events :as events]
   [cljs-dm-client.miscellaneous.subs :as subs]
   ["reactstrap/lib/Modal" :default Modal]
   ["reactstrap/lib/ModalBody" :default ModalBody]
   ["reactstrap/lib/ModalFooter" :default ModalFooter]
   ["reactstrap/lib/ModalHeader" :default ModalHeader]))

(def CATEGORY_MODAL_KEY :category-modal)

(defn category-modal []
  (let [category @(subscribe [:edit-object :edit-category])]
    [:> Modal {:is-open  @(subscribe [:modal-open? CATEGORY_MODAL_KEY])
               :toggle   #(dispatch [:toggle-modal CATEGORY_MODAL_KEY])
               :size     :lg
               :backdrop :static}
     [:> ModalHeader
      (if (:id category)
        "Update Category"
        "Add Category")]
     [:> ModalBody {:class :modal-body}
      [text-input-row "Name" 100 category "category" :name]]
     [:> ModalFooter {:class :modal-footer-buttons}
      [:button.action-link {:on-click #(dispatch [:toggle-modal CATEGORY_MODAL_KEY])}
       "Cancel"]
      [:button.action-link {:on-click #(dispatch [::events/delete-category (:id category)])}
       "Delete"]
      [:button.action-button {:on-click #(dispatch [::events/edit-category category])}
       "Save"]]]))

(defn category-notes []
  (let [type "MISCELLANEOUS"
        selected-category @(subscribe [::subs/selected-category])
        notes @(subscribe [:notes-for-ref type (:id selected-category)])]
    [standard-note-columns {:notes           notes
                            :container-class :notes-container
                            :selected-obj    selected-category
                            :obj-type        type}]))

(defn category-details []
  (when-let [selected-category @(subscribe [::subs/selected-category])]
    [:div.locations-page.panel-content
     [:div.panel-content
      [logical-division {:text (:name selected-category)
                         :class :no-bottom}]
      [category-notes]]]))

(defn miscellaneous-content []
  (let [categories @(subscribe [::subs/categories])]
    [:<>
     [category-details]
     [:div.right-panel
      [logical-division {:left [:button {:class [:action-button :skinny]
                                         :on-click #(dispatch [::events/open-edit-category-modal nil])}
                                "Add Category"]
                         :text "Categories"}]
      (into [:<>]
            (for [category categories]
              [:button.action-button {:on-click #(dispatch [::events/select-category (:id category)])}
               (:name category)]))]]))

(defn miscellaneous []
  [:<>
   [note-modal]
   [category-modal]
   [loading-wrapper
    {:container [campaign-panel]
     :content   [miscellaneous-content]}]])

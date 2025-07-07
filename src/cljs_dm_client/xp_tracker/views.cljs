(ns cljs-dm-client.xp-tracker.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [cljs-dm-client.components.components :refer [logical-division]]
    [cljs-dm-client.components.forms :refer [checkbox-input-row
                                             number-input-row
                                             textarea-input-row]]
    [cljs-dm-client.components.notes :refer [note-modal]]
    [cljs-dm-client.layout.views :refer [campaign-panel
                                         loading-wrapper]]
    [cljs-dm-client.utils :as utils]
    [cljs-dm-client.xp-tracker.events :as events]
    [cljs-dm-client.xp-tracker.subs :as subs]
    ["reactstrap/lib/Modal" :default Modal]
    ["reactstrap/lib/ModalBody" :default ModalBody]
    ["reactstrap/lib/ModalFooter" :default ModalFooter]
    ["reactstrap/lib/ModalHeader" :default ModalHeader]))

(def XP_MODAL_KEY :xp-modal)

(defn xp-modal []
      (let [xp @(subscribe [:edit-object :edit-xp])]
           [:> Modal {:is-open @(subscribe [:modal-open? XP_MODAL_KEY])
                      :toggle  #(dispatch [:toggle-modal XP_MODAL_KEY])
                      :size    :md}
            [:> ModalHeader
             (if (:id xp)
               "Update XP"
               "Add XP")]
            [:> ModalBody {:class :modal-body}
             [textarea-input-row "Description" 1000 4 xp "xp" :description]
             [:div.input-row
              [:button.action-button {:on-click #(dispatch [::events/calculate-xp])}
               "Calculate XP"]]
             [number-input-row "XP" 0 999999 xp "xp" :xp]
             [checkbox-input-row "Finalized?" xp "xp" :finalized]]
            [:> ModalFooter {:class :modal-footer-buttons}
             [:button.action-link {:on-click #(dispatch [:toggle-modal XP_MODAL_KEY])}
              "Cancel"]
             [:button.action-link {:on-click #(dispatch [::events/delete-xp (:id xp)])}
              "Delete"]
             [:button.action-button {:on-click #(dispatch [::events/edit-xp xp])}
              "Save"]]]))

(defn build-xp-rows [xps]
      (if (seq xps)
        (into [:div.common-panel]
              (for [xp xps]
                   [:div.xp-row
                    [:span (:description xp)]
                    [:span
                     (:xp xp)
                     (when-not (:finalized xp)
                               [:button.edit-button {:on-click #(dispatch [::events/open-edit-xp-modal xp])}])]]))
        [:div.common-panel "No XPs"]))

(defn percentage-row [current goal]
      (let [percent (utils/percentage current goal)]
           [:<>
            [:div percent "% - " current " / " goal]
            [:progress {:max 100 :value percent}]]))

(defn summary []
      (let [campaign                      @(subscribe [:selected-campaign])
            planned-xp-diff               @(subscribe [::subs/adjusted-planned-xp-total])
            current-level                 (utils/level-by-xp (:current-player-xp campaign))
            adjusted-current-xp           (- (:current-player-xp campaign) (:xp current-level))
            next-level                    (utils/next-level-by-xp (:current-player-xp campaign))
            adjusted-next-xp              (- (:xp next-level) (:xp current-level))
            current-with-planned          (+ planned-xp-diff (:current-player-xp campaign))
            adjusted-current-with-planned (+ planned-xp-diff adjusted-current-xp)]
           [:div.common-panel
            [:h4 "Current Level: " (:lvl current-level)]
            [:div (:current-player-xp campaign) " / " (:xp next-level)]
            [percentage-row adjusted-current-xp adjusted-next-xp]
            [:hr]
            (if (>= current-with-planned (:xp next-level))
              (let [level-after-next (utils/next-level-by-xp current-with-planned)
                    future-planned   (- current-with-planned (:xp next-level))
                    future-goal      (- (:xp level-after-next) (:xp next-level))]
                   [:<>
                    [:h4 "With Planned: " (:lvl next-level)]
                    [:div current-with-planned " / " (:xp level-after-next)]
                    [percentage-row future-planned future-goal]])
              [:<>
               [:h4 "With Planned: " (:lvl current-level)]
               [:div current-with-planned " / " (:xp next-level)]
               [percentage-row adjusted-current-with-planned adjusted-next-xp]])]))

(defn planned-xp []
      (let [xps @(subscribe [::subs/planned-xp])]
           (build-xp-rows xps)))

(defn finalized-xp []
      (let [xps @(subscribe [::subs/finalized-xp])]
           (build-xp-rows xps)))

(defn xp-tracker-content []
      [:<>
       [:div.panel-content
        [summary]
        [logical-division {:left [:button {:class [:action-button :skinny]
                                           :on-click #(dispatch [::events/open-edit-xp-modal nil])}
                                  "Add XP"]
                           :text "Planned XP"}]
        [planned-xp]]
       [:div.right-panel
        [logical-division {:text "Finalized XP"}]
        [finalized-xp]]])

(defn xp-tracker []
      [:<>
       [note-modal]
       [xp-modal]
       [loading-wrapper
        {:container [campaign-panel]
         :content   [xp-tracker-content]}]])

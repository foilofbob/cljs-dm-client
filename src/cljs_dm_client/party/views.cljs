(ns cljs-dm-client.party.views
  (:require
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-dm-client.components.components :refer [build-notes
                                                 note-modal]]
   [cljs-dm-client.layout.views :refer [campaign-panel
                                        loading-wrapper]]
   [cljs-dm-client.party.events :as events]
   [cljs-dm-client.party.subs :as subs]
   ["reactstrap/lib/Modal" :default Modal]
   ["reactstrap/lib/ModalBody" :default ModalBody]
   ["reactstrap/lib/ModalFooter" :default ModalFooter]
   ["reactstrap/lib/ModalHeader" :default ModalHeader]
   ["reactstrap/lib/UncontrolledTooltip" :default UncontrolledTooltip]
   ))

(def ITEM_MODAL_KEY :item-modal)

(defn text-input-row [label length obj obj-type obj-key]
      (let [input-id (str obj-type "-" (or (:id obj) "new") "-" (name obj-key))]
           [:div
            [:label {:for input-id}
             label]
            [:input {:id         input-id
                     :value      (obj-key obj)
                     :max-length length
                     :on-change  #(dispatch [:update-edit-field :edit-item obj-key (-> % .-target .-value)])}]]))

(defn item-modal []
      (let [item @(subscribe [:edit-object :edit-item])]
           [:> Modal {:is-open @(subscribe [:modal-open? ITEM_MODAL_KEY])
                      :toggle  #(dispatch [:toggle-modal ITEM_MODAL_KEY])
                      :size    :xl}
            [:> ModalHeader
             (if (:id item)
               "Update Item"
               "Add Item")]
            [:> ModalBody {:class :modal-body-note} ;; TODO: Generic class?
             [text-input-row "Name" 100 item "item" :name]
             [:textarea {:value      (:description item)
                         :max-length 10000
                         :rows       4
                         :on-change  #(dispatch [:update-edit-field :edit-item :description (-> % .-target .-value)])}]
             [text-input-row "Link" 500 item "item" :link]
             [text-input-row "Rarity" 10 item "item" :rarity]
             [text-input-row "Cost" 20 item "item" :cost]
             [text-input-row "Requirements" 64 item "item" :requirements]
             ;; Is Container - Toggle? Yes/No?
             ;; Carried By - Select: null, Player, Item
             ;; Carried By ID - List of available objects
             ]
            [:> ModalFooter {:class :modal-footer-buttons}
             [:button.action-link {:on-click #(dispatch [:toggle-modal ITEM_MODAL_KEY])}
              "Cancel"]
             [:button.action-link {:on-click #(dispatch [::delete-item (:id item)])}
              "Delete"]
             [:button.action-button {:on-click #(dispatch [::edit-item item])}
              "Save"]]]))

(defn player-attribute [label value]
      [:div.attribute
       [:span.attribute-header label]
       [:span.attribute-value value]])

(defn player-attributes [player]
      [:div.player-attributes
       [player-attribute "AC" (:armor-class player)]
       [player-attribute "HP" (:hit-points player)]
       [player-attribute "PP" (:passive-perception player)]
       [player-attribute "MV" (:movement player)]])

(defn party-content []
      (let [players @(subscribe [::subs/players])
            notes   @(subscribe [:notes])]
           [:<>
            [:div.party
             (into [:div.panel-content]
                  (for [player players]
                       [:div.player-card
                        [:div.player-header
                         [:div
                          [:div.player-name (:name player)]
                          [:div (str (:race player) ", " (:class player))]]
                         [player-attributes player]]
                        [:hr]
                        [:div [:strong "Languages: "] (:languages player)]
                        [:hr]
                        (into [:<>]
                              (or (some->> notes
                                           (filter #(= (:id player) (:reference-id %)))
                                           seq
                                           build-notes)
                                  [[:button.action-link {:on-click #(dispatch [:open-edit-note-modal player "PLAYER" ""])}
                                   "Add Note"]
                                   [:hr]]))
                        [:div
                         ;; Player Items
                         ]
                        ]))]
            [:div.right-panel
             [:button.action-button {:on-click #(dispatch [::events/open-edit-item-modal nil])}
              "Add Item"]
             ]
            ]
           ))

(defn party []
      [:<>
       [note-modal]
       [item-modal]
       [loading-wrapper
        {:container [campaign-panel]
         :content   [party-content]}]])


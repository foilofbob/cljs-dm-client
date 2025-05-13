(ns cljs-dm-client.party.views
  (:require
   [clojure.string :refer [lower-case]]
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
   ["reactstrap/lib/UncontrolledTooltip" :default UncontrolledTooltip]))

(def ITEM_MODAL_KEY :item-modal)

(defn elem-id [obj-type obj-id obj-key]
      (str obj-type "-" (or obj-id "new") "-" (name obj-key)))

(defn text-input-row [label length obj obj-type obj-key]
      (let [input-id (elem-id obj-type (:id obj) obj-key)]
           [:div.input-row
            [:label {:for input-id}
             label]
            [:input {:id         input-id
                     :value      (obj-key obj)
                     :max-length length
                     :on-change  #(dispatch [:update-edit-field :edit-item obj-key (-> % .-target .-value)])}]]))

(defn textarea-input-row [label length rows obj obj-type obj-key]
      (let [input-id (elem-id obj-type (:id obj) obj-key)]
           [:div.input-row
            [:label {:for input-id}
             label]
            [:textarea {:id         input-id
                        :value      (obj-key obj)
                        :max-length length
                        :rows       rows
                        :on-change  #(dispatch [:update-edit-field :edit-item obj-key (-> % .-target .-value)])}]]))

(defn checkbox-input-row [label obj obj-type obj-key]
      (let [input-id (elem-id obj-type (:id obj) obj-key)]
           [:div.input-row
            [:label {:for input-id}
             label]
            [:input {:id         input-id
                     :type       :checkbox
                     :value      (obj-key obj)
                     :on-change  #(dispatch [:update-edit-field :edit-item obj-key (-> % .-target .-checked)])}]]))

(defn select-input-row [label obj obj-type obj-key options]
      (let [input-id (elem-id obj-type (:id obj) obj-key)]
           [:div.input-row
            [:label {:for input-id}
             label]
            (into [:select {:id         input-id
                            :value      (obj-key obj)
                            :on-change  #(dispatch [:update-edit-field :edit-item obj-key (-> % .-target .-value)])}]
                  (for [option options]
                       [:option {:value (:value option)} (:label option)]))]))

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
             [textarea-input-row "Description" 5000 4 item "item" :description]
             [text-input-row "Link" 500 item "item" :link]
             [text-input-row "Rarity" 10 item "item" :rarity]
             [text-input-row "Cost" 20 item "item" :cost]
             [text-input-row "Requirements" 64 item "item" :requirements]
             [checkbox-input-row "Is this a container?" item "item" :is-container]
             [select-input-row "Carried by / Stored in" item "item" :carried-by
              [{:label "- Not Carried -" :value ""}
               {:label "Item" :value "ITEM"}
               {:label "Player" :value "PLAYER"}]]
             (when (:carried-by item)
                   (let [options (if (= "PLAYER" (:carried-by item))
                                   @(subscribe [::subs/players-as-select-options])
                                   @(subscribe [::subs/containers-as-select-options]))]
                        [select-input-row "Belongs to..." item "item" :carried-by-id options]))]
            [:> ModalFooter {:class :modal-footer-buttons}
             [:button.action-link {:on-click #(dispatch [:toggle-modal ITEM_MODAL_KEY])}
              "Cancel"]
             [:button.action-link {:on-click #(dispatch [::events/delete-item (:id item)])}
              "Delete"]
             [:button.action-button {:on-click #(dispatch [::events/edit-item item])}
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

(defn item-component [item]
      [:div.item {:key (str "item-" (:id item))}
       [:span.name {:class (some-> item :rarity lower-case)}
        (str (:name item))]
       (when (seq (:link item))
             [:a.dnd-icon {:href (:link item)
                  :target "_blank"}
              [:div.dnd-icon]])
       [:button.edit-button {:on-click #(dispatch [::events/open-edit-item-modal item])}]])

(defn party-content []
      (let [players             @(subscribe [::subs/players])
            notes               @(subscribe [:notes])
            items-not-carried   @(subscribe [::subs/items-not-carried])
            player-items        @(subscribe [::subs/items-carried-by-players])
            items-as-containers @(subscribe [::subs/items-as-containers])
            container-items     @(subscribe [::subs/items-carried-by-items])]
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
                        [:div [:strong "Inventory"]]
                        [:div.items
                         (into [:<>]
                               (->> player-items
                                    (filter #(= (:id player) (:carried-by-id %)))
                                    (sort-by :name)
                                    (map item-component)))]]))
             [:hr.section-divider]
             (into [:div.panel-content]
                   (for [container items-as-containers]
                        [:div.player-card
                         [:div.player-header
                          [:div.player-name (:name container)]]
                         [:hr]
                         [:div.items
                          (into [:<>]
                                (->> container-items
                                     (filter #(= (:id container) (:carried-by-id %)))
                                     (sort-by :name)
                                     (map item-component)))]]))]
            [:div.right-panel
             [:button.action-button {:on-click #(dispatch [::events/open-edit-item-modal nil])}
              "Add Item"]
             [:hr.section-divider]
             (map item-component items-not-carried)]]))

(defn party []
      [:<>
       [note-modal]
       [item-modal]
       [loading-wrapper
        {:container [campaign-panel]
         :content   [party-content]}]])


(ns cljs-dm-client.party.views
  (:require
   [clojure.string :refer [lower-case]]
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-dm-client.components.components :refer [logical-division]]
   [cljs-dm-client.components.forms :refer [text-input-row
                                            number-input-row
                                            textarea-input-row
                                            checkbox-input-row
                                            select-input-row]]
   [cljs-dm-client.components.notes :refer [build-notes
                                            note-modal]]
   [cljs-dm-client.components.markdown :refer [markdown-div]]
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
(def PLAYER_MODAL_KEY :player-modal)

(defn item-modal []
  (let [item @(subscribe [:edit-object :edit-item])]
    [:> Modal {:is-open @(subscribe [:modal-open? ITEM_MODAL_KEY])
               :toggle  #(dispatch [:toggle-modal ITEM_MODAL_KEY])
               :size    :xl}
     [:> ModalHeader
      (if (:id item)
        "Update Item"
        "Add Item")]
     [:> ModalBody {:class :modal-body}
      [text-input-row "Name" 100 item "item" :name]
      [textarea-input-row "Description" 5000 15 item "item" :description]
      [text-input-row "Link" 500 item "item" :link]
      [text-input-row "Rarity" 10 item "item" :rarity]
      [text-input-row "Cost" 20 item "item" :cost]
      [text-input-row "Requirements" 64 item "item" :requirements]
      [checkbox-input-row "Is this a container?" item "item" :is-container]
      [select-input-row "Carried by / Stored in" item "item" :carried-by
       [{:label "- Not Carried -" :value ""}
        {:label "Item" :value "ITEM"}
        {:label "Player" :value "PLAYER"}]]
      (when (seq (:carried-by item))
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

(defn player-modal []
  (let [player @(subscribe [:edit-object :edit-player])]
    [:> Modal {:is-open @(subscribe [:modal-open? PLAYER_MODAL_KEY])
               :toggle  #(dispatch [:toggle-modal PLAYER_MODAL_KEY])
               :size    :xl}
     [:> ModalHeader
      (if (:id player)
        "Update Player"
        "Add Player")]
     [:> ModalBody {:class :modal-body}
      [text-input-row "Name" 100 player "player" :name]
      [text-input-row "Race" 100 player "player" :race]
      [text-input-row "Class" 100 player "player" :class]
      [number-input-row "Armor Class" 0 30 player "player" :armor-class]
      [number-input-row "Hit Points" 0 500 player "player" :hit-points]
      [number-input-row "Passive Perception" 0 30 player "player" :passive-perception]
      [text-input-row "Languages" 100 player "player" :languages]
      [number-input-row "Movement" 0 200 player "player" :movement]]
     [:> ModalFooter {:class :modal-footer-buttons}
      [:button.action-link {:on-click #(dispatch [:toggle-modal PLAYER_MODAL_KEY])}
       "Cancel"]
      [:button.action-link {:on-click #(dispatch [::events/delete-player (:id player)])}
       "Delete"]
      [:button.action-button {:on-click #(dispatch [::events/edit-player player])}
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
  (let [element-id (str "item-" (:id item))]
    [:<>
     [:div.item {:key element-id}
      [:span.name {:id    element-id
                   :class (some-> item :rarity lower-case)}
       (str (:name item))]
      (when (seq (:link item))
        [:a.dnd-icon {:href (:link item)
                      :target "_blank"}
         [:div.dnd-icon]])
      [:button.edit-button {:on-click #(dispatch [::events/open-edit-item-modal item])}]]
     (when (seq (:description item))
       [:> UncontrolledTooltip {:target           element-id
                                :placement        "bottom"
                                :inner-class-name "component-tooltip wide-description"}
        [markdown-div (:description item)]])]))

(defn party-content []
  (let [players             @(subscribe [::subs/players])
        notes               @(subscribe [:notes])
        items-not-carried   @(subscribe [::subs/items-not-carried])
        player-items        @(subscribe [::subs/items-carried-by-players])
        items-as-containers @(subscribe [::subs/items-as-containers])
        container-items     @(subscribe [::subs/items-carried-by-items])]
    [:<>
     [:div.party.panel-content
      [:div.panel-content
       [:button.action-button {:on-click #(dispatch [::events/open-edit-player-modal nil])}
        "Add Player"]]
      (into [:div.panel-content]
            (for [player players]
              [:div.player-card
               [:div.player-header
                [:div
                 [:div.player-name (:name player)]
                 [:div (str (:race player) ", " (:class player))]]
                [player-attributes player]
                [:button.edit-button {:on-click #(dispatch [::events/open-edit-player-modal player])}]]
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
                           (map item-component)))]]))
      [logical-division {:text "Items With Storage"}]
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
                           (map item-component)))]]))]
     [:div.right-panel
      [logical-division {:left [:button {:class [:action-button :skinny]
                                         :on-click #(dispatch [::events/open-edit-item-modal nil])}
                                "Add Item"]
                         :text "Unassigned Items"}]
      (into [:<>]
            (map item-component items-not-carried))]]))

(defn party []
  [:<>
   [note-modal]
   [item-modal]
   [player-modal]
   [loading-wrapper
    {:container [campaign-panel]
     :content   [party-content]}]])


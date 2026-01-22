(ns cljs-dm-client.characters.views
  (:require
   [clojure.string :refer [lower-case]]
   [re-frame.core :refer [dispatch subscribe]]
   [camel-snake-kebab.core :as csk]
   [cljs-dm-client.components.components :refer [logical-division]]
   [cljs-dm-client.components.forms :refer [build-options
                                            number-input-row
                                            text-input-row
                                            textarea-input-row
                                            checkbox-input-row
                                            select-input-row]]
   [cljs-dm-client.components.notes :refer [build-notes
                                            note-modal]]
   [cljs-dm-client.components.markdown :refer [markdown-div]]
   [cljs-dm-client.characters.events :as events]
   [cljs-dm-client.characters.subs :as subs]
   [cljs-dm-client.utils :as utils]
   ["reactstrap/lib/Modal" :default Modal]
   ["reactstrap/lib/ModalBody" :default ModalBody]
   ["reactstrap/lib/ModalFooter" :default ModalFooter]
   ["reactstrap/lib/ModalHeader" :default ModalHeader]
   ["reactstrap/lib/UncontrolledTooltip" :default UncontrolledTooltip]))

(def CHARACTER_MODAL_KEY :player-modal)
(def ITEM_MODAL_KEY :item-modal)
(def SPELLBOOK_MODAL_KEY :spellbook-modal)

(def proficiency-options
  [{:label "0" :value 0}
   {:label "1/2" :value 0.5}
   {:label "1" :value 1.0}
   {:label "1-1/2" :value 1.5}
   {:label "2" :value 2.0}])

(def player-options
  [{:label "Player" :value "PLAYER"}
   {:label "NPC" :value "NPC"}])

(defn item-modal []
  (let [item @(subscribe [:edit-object :edit-item])
        base {:obj item :obj-type "item"}]
    [:> Modal {:is-open  @(subscribe [:modal-open? ITEM_MODAL_KEY])
               :toggle   #(dispatch [:toggle-modal ITEM_MODAL_KEY])
               :size     :xl
               :backdrop :static}
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
      [checkbox-input-row (assoc base :label "Is this a container?" :obj-key :is-container)]
      [select-input-row "Carried by / Stored in" item "item" :carried-by
       [{:label "- Not Carried -" :value ""}
        {:label "Item" :value "ITEM"}
        {:label "Player" :value "PLAYER"}
        {:label "NPC" :value "NPC"}]]
      (when (seq (:carried-by item))
        (let [options (case (:carried-by item)
                        "PLAYER" @(subscribe [::subs/players-as-select-options])
                        "NPC" @(subscribe [::subs/npcs-as-select-options])
                        "ITEM" @(subscribe [::subs/containers-as-select-options]))]
          [select-input-row "Belongs to..." item "item" :carried-by-id options]))]
     [:> ModalFooter {:class :modal-footer-buttons}
      [:button.action-link {:on-click #(dispatch [:toggle-modal ITEM_MODAL_KEY])}
       "Cancel"]
      [:button.action-link {:on-click #(dispatch [::events/delete-item (:id item)])}
       "Delete"]
      [:button.action-button {:on-click #(dispatch [::events/edit-item item])}
       "Save"]]]))

(defn character-modal []
  (let [player @(subscribe [:edit-object :edit-player])
        base {:obj player :obj-type "player"}]
    [:> Modal {:is-open  @(subscribe [:modal-open? CHARACTER_MODAL_KEY])
               :toggle   #(dispatch [:toggle-modal CHARACTER_MODAL_KEY])
               :size     :lg
               :backdrop :static}
     [:> ModalHeader
      (if (:id player)
        "Update Character"
        "Add Character")]
     [:> ModalBody {:class :modal-body}
      [select-input-row "Player Type" player "player" :player-type player-options]
      [text-input-row "Name" 100 player "player" :name]
      [text-input-row "Race" 100 player "player" :race]
      [text-input-row "Class" 100 player "player" :class]
      [select-input-row {:label    "Level - defaults to current party level if left as 0"
                         :numeric? true
                         :obj      player
                         :obj-key  :level
                         :obj-type "player"
                         :options  (mapv (fn [n] {:label n :value n}) (range 25))}]

      [:div.input-group
       [number-input-row (merge base {:label "Hit Points"
                                      :max 500
                                      :obj-key :hit-points})]
       [number-input-row (merge base {:label "Armor Class"
                                      :max 30
                                      :obj-key :armor-class})]
       [number-input-row (merge base {:label "Passive Perception"
                                      :max 30
                                      :obj-key :passive-perception})]
       [number-input-row (merge base {:label "Movement"
                                      :max 200
                                      :obj-key :movement})]]

      [:div.input-group
       [number-input-row (merge base {:label "Strength"
                                      :max 40
                                      :obj-key :strength})]
       [number-input-row (merge base {:label "Dexterity"
                                      :max 40
                                      :obj-key :dexterity})]
       [number-input-row (merge base {:label "Constitution"
                                      :max 40
                                      :obj-key :constitution})]
       [number-input-row (merge base {:label "Intelligence"
                                      :max 40
                                      :obj-key :intelligence})]
       [number-input-row (merge base {:label "Wisdom"
                                      :max 40
                                      :obj-key :wisdom})]
       [number-input-row (merge base {:label "Charisma"
                                      :max 40
                                      :obj-key :charisma})]]

      [:hr]
      [:strong "Save Proficiencies"]
      [:div.input-group
       [checkbox-input-row (merge base {:label "Strength"
                                        :obj-key :strength-save-proficiency})]
       [checkbox-input-row (merge base {:label "Dexterity"
                                        :obj-key :dexterity-save-proficiency})]
       [checkbox-input-row (merge base {:label "Constitution"
                                        :obj-key :constitution-save-proficiency})]
       [checkbox-input-row (merge base {:label "Intelligence"
                                        :obj-key :intelligence-save-proficiency})]
       [checkbox-input-row (merge base {:label "Wisdom"
                                        :obj-key :wisdom-save-proficiency})]
       [checkbox-input-row (merge base {:label "Charisma"
                                        :obj-key :charisma-save-proficiency})]]

      [:hr]
      [:strong "Skill Proficiencies"]
      (let [fields [["Acrobatics" :acrobatics-proficiency-bonus]
                    ["Animal Handling" :animal-handling-proficiency-bonus]
                    ["Arcana" :arcana-proficiency-bonus]
                    ["Athletics" :athletics-proficiency-bonus]
                    ["Deception" :deception-proficiency-bonus]
                    ["History" :history-proficiency-bonus]
                    ["Insight" :insight-proficiency-bonus]
                    ["Intimidation" :intimidation-proficiency-bonus]
                    ["Investigation" :investigation-proficiency-bonus]
                    ["Medicine" :medicine-proficiency-bonus]
                    ["Nature" :nature-proficiency-bonus]
                    ["Perception" :perception-proficiency-bonus]
                    ["Performance" :performance-proficiency-bonus]
                    ["Persuasion" :persuasion-proficiency-bonus]
                    ["Religion" :religion-proficiency-bonus]
                    ["Sleight of Hand" :sleight-of-hand-proficiency-bonus]
                    ["Stealth" :stealth-proficiency-bonus]
                    ["Survival" :survival-proficiency-bonus]]]
        (into [:div.input-group]
              (map (fn [[label field]]
                     [select-input-row label player "player" field proficiency-options])
                   fields)))
      [:hr]
      [text-input-row "Languages" 100 player "player" :languages]
      [text-input-row "Proficiencies" 256 player "player" :proficiencies]]
     [:> ModalFooter {:class :modal-footer-buttons}
      [:button.action-link {:on-click #(dispatch [:toggle-modal CHARACTER_MODAL_KEY])}
       "Cancel"]
      [:button.action-link {:on-click #(dispatch [::events/delete-player (:id player)])}
       "Delete"]
      [:button.action-button {:on-click #(dispatch [::events/edit-player player])}
       "Save"]]]))

(defn modifier [attribute]
  (-> attribute
      (- 10)
      (/ 2)
      js/Math.trunc))

(defn modified-value [attribute-value proficiency-modifier proficiency-bonus]
  (-> attribute-value modifier (+ (* proficiency-modifier proficiency-bonus)) Math/floor))

(defn attribute-save [attribute-value proficient? proficiency-bonus]
  (modified-value attribute-value (if proficient? 1 0) proficiency-bonus))

(defn player-attribute
  ([label value]
   (player-attribute label value nil))
  ([label value subtext]
   [:div.attribute
    [:span.attribute-text label]
    [:span.attribute-value (or value " -- ")]
    (when subtext
      [:span.attribute-text subtext])]))

(defn stats [player]
  [:div.player-attributes
   [player-attribute "AC" (:armor-class player)]
   [player-attribute "HP" (:hit-points player)]
   [player-attribute "PP" (:passive-perception player)]
   [player-attribute "MV" (:movement player)]])

(defn attributes [{:keys [strength dexterity constitution intelligence wisdom charisma]}]
  [:div.player-attributes
   [player-attribute "Str" strength (modifier strength)]
   [player-attribute "Dex" dexterity (modifier dexterity)]
   [player-attribute "Con" constitution (modifier constitution)]
   [player-attribute "Int" intelligence (modifier intelligence)]
   [player-attribute "Wis" wisdom (modifier wisdom)]
   [player-attribute "Cha" charisma (modifier charisma)]])

(defn item-component [item]
  (let [element-id (str "item-" (:id item))]
    [:<>
     [:div.item {:key element-id}
      [:span.name {:id    element-id
                   :class (some-> item :rarity csk/->kebab-case-keyword)}
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

(defn saves [player proficiency]
  [:div.player-attributes
   [:strong "Saves: "]
   [player-attribute "Str" (attribute-save (:strength player) (:strength-save-proficiency player) proficiency)]
   [player-attribute "Dex" (attribute-save (:dexterity player) (:dexterity-save-proficiency player) proficiency)]
   [player-attribute "Con" (attribute-save (:constitution player) (:constitution-save-proficiency player) proficiency)]
   [player-attribute "Int" (attribute-save (:intelligence player) (:intelligence-save-proficiency player) proficiency)]
   [player-attribute "Wis" (attribute-save (:wisdom player) (:wisdom-save-proficiency player) proficiency)]
   [player-attribute "Cha" (attribute-save (:charisma player) (:charisma-save-proficiency player) proficiency)]])

(defn skills [player proficiency]
  (let [{:keys [strength dexterity constitution intelligence wisdom charisma]} player
        skill-fn (fn [v k] (modified-value v (k player) proficiency))]
    [:div.skills-container
     [:div.skills-column
      [:p
       [:span "Acrobatics"]
       [:span (skill-fn dexterity :acrobatics-proficiency-bonus)]]
      [:p
       [:span "Animal Handling"]
       [:span (skill-fn wisdom :animal-handling-proficiency-bonus)]]
      [:p
       [:span "Arcana"]
       [:span (skill-fn intelligence :arcana-proficiency-bonus)]]
      [:p
       [:span "Athletics"]
       [:span (skill-fn strength :athletics-proficiency-bonus)]]
      [:p
       [:span "Deception"]
       [:span (skill-fn charisma :deception-proficiency-bonus)]]]
     [:div.skills-column
      [:p
       [:span "History"]
       [:span (skill-fn intelligence :history-proficiency-bonus)]]
      [:p
       [:span "Insight"]
       [:span (skill-fn wisdom :insight-proficiency-bonus)]]
      [:p
       [:span "Intimidation"]
       [:span (skill-fn charisma :intimidation-proficiency-bonus)]]
      [:p
       [:span "Investigation"]
       [:span (skill-fn intelligence :investigation-proficiency-bonus)]]
      [:p
       [:span "Medicine"]
       [:span (skill-fn wisdom :medicine-proficiency-bonus)]]]
     [:div.skills-column
      [:p
       [:span "Nature"]
       [:span (skill-fn intelligence :nature-proficiency-bonus)]]
      [:p
       [:span "Perception"]
       [:span (skill-fn wisdom :perception-proficiency-bonus)]]
      [:p
       [:span "Performance"]
       [:span (skill-fn charisma :performance-proficiency-bonus)]]
      [:p
       [:span "Persuasion"]
       [:span (skill-fn charisma :persuasion-proficiency-bonus)]]
      [:p
       [:span "Religion"]
       [:span (skill-fn intelligence :religion-proficiency-bonus)]]]
     [:div.skills-column
      [:p
       [:span "Sleight of Hand"]
       [:span (skill-fn dexterity :sleight-of-hand-proficiency-bonus)]]
      [:p
       [:span "Stealth"]
       [:span (skill-fn dexterity :stealth-proficiency-bonus)]]
      [:p
       [:span "Survival"]
       [:span (skill-fn wisdom :survival-proficiency-bonus)]]]]))

(defn spellbook-modal []
      (let [spellbook @(subscribe [:edit-object :edit-spellbook])
            base {:obj spellbook :obj-type "spellbook"}]
           [:> Modal {:is-open  @(subscribe [:modal-open? SPELLBOOK_MODAL_KEY])
                      :toggle   #(dispatch [:toggle-modal SPELLBOOK_MODAL_KEY])
                      :size     :lg
                      :backdrop :static}
            [:> ModalHeader
             (if (:id spellbook)
               "Update Spell Casting"
               "Add Spell Casting")]
            [:> ModalBody {:class :modal-body}
             [textarea-input-row "Spell Stats" 5000 15 spellbook "spellbook" :spell-stats]]
            [:> ModalFooter {:class :modal-footer-buttons}
             [:button.action-link {:on-click #(dispatch [:toggle-modal SPELLBOOK_MODAL_KEY])}
              "Cancel"]
             [:button.action-link {:on-click #(dispatch [::events/delete-spellbook (:id spellbook)])}
              "Delete"]
             [:button.action-button {:on-click #(dispatch [::events/edit-spellbook spellbook])}
              "Save"]]]))

(defn character-content [player-characters?]
  (let [notes               @(subscribe [:notes])
        items-not-carried   @(subscribe [::subs/items-not-carried])
        items-as-containers (when player-characters? @(subscribe [::subs/items-as-containers]))
        container-items     (when player-characters? @(subscribe [::subs/items-carried-by-items]))
        current-party-level @(subscribe [::subs/current-party-level])
        characters          (if player-characters?
                              @(subscribe [::subs/players])
                              @(subscribe [::subs/npcs]))
        character-items     (if player-characters?
                              @(subscribe [::subs/items-carried-by-players])
                              @(subscribe [::subs/items-carried-by-npcs]))
        spellbooks          @(subscribe [::subs/spellbooks])]
    [:<>
     [:div.party.panel-content
      [:div.panel-content
       [:button.action-button {:on-click #(dispatch [::events/open-edit-player-modal nil])}
        "Add Character"]]
      (into [:div.panel-content]
            (for [character characters]
              (let [character-level (if (some-> character :level (> 0))
                                      (-> character :level utils/level-by-level)
                                      current-party-level)
                    spellbook       (some->> spellbooks
                                             (filter #(= (:character-id %) (:id character)))
                                             first)]
                [:div.player-card
                 [:div.player-header
                  [:div
                   [:div.player-name (:name character)]
                   [:div (str (:race character) ", " (:class character) " (lvl " (:lvl character-level) ")")]]
                  [stats character]
                  [:button.edit-button {:on-click #(dispatch [::events/open-edit-player-modal character])}]]
                 [:hr]
                 [:div.attribute-row
                  [attributes character]
                  [saves character (:proficiency character-level)]]
                 [:hr]
                 [:div [:strong "Proficiency Bonus: "] (:proficiency character-level)]
                 [:div [:strong "Languages: "] (:languages character)]
                 [:div [:strong "Proficiencies: "] (:proficiencies character)]
                 [:hr]
                 [skills character (:proficiency character-level)]
                 [:hr]
                 (into [:<>]
                       (or (some->> notes
                                    (filter #(= (:id character) (:reference-id %)))
                                    seq
                                    build-notes)
                           [[:button.action-link {:on-click #(dispatch [:open-edit-note-modal character "CHARACTER" ""])}
                             "Add Note"]
                            [:hr]]))

                 (if (seq spellbook)
                   [:<>
                    [:div.spell-casting
                     [:strong "Spell Casting"]
                     [:button.edit-button {:on-click #(dispatch [::events/open-edit-spellbook-modal (:id character) spellbook])}]]
                    [markdown-div (:spell-stats spellbook)]
                    ;; TODO: Spell table
                    ]
                   [:button.action-link {:on-click #(dispatch [::events/open-edit-spellbook-modal (:id character) nil])}
                    "Add Spell Casting"])
                 [:hr]

                 [:div [:strong "Inventory"]]
                 [:div.items
                  (into [:<>]
                        (->> character-items
                             (filter #(= (:id character) (:carried-by-id %)))
                             (map item-component)))]])))
      (when (seq items-as-containers)
        [:<>
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
                              (map item-component)))]]))])]
     [:div.right-panel
      [logical-division {:left [:button {:class [:action-button :skinny]
                                         :on-click #(dispatch [::events/open-edit-item-modal nil])}
                                "Add Item"]
                         :text "Unassigned Items"}]
      (into [:<>]
            (map item-component items-not-carried))]]))

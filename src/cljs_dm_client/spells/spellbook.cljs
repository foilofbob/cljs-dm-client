(ns cljs-dm-client.spells.spellbook
  (:require
   ["reactstrap/lib/Modal" :default Modal]
   ["reactstrap/lib/ModalBody" :default ModalBody]
   ["reactstrap/lib/ModalFooter" :default ModalFooter]
   ["reactstrap/lib/ModalHeader" :default ModalHeader]
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx subscribe]]
   [cljs-dm-client.spells.events :as events]
   [cljs-dm-client.spells.subs :as subs]))

(def SPELL_MODAL_KEY :spell-modal)

(defn add-spell-modal []
      (let [spellbook-id      @(subscribe [:edit-object :edit-spellbook-id])
            spells            @(subscribe [::subs/spells])
            spell-search-text @(subscribe [::subs/spell-search-text])]
           [:> Modal {:is-open  @(subscribe [:modal-open? SPELL_MODAL_KEY])
                      :toggle   #(dispatch [:toggle-modal SPELL_MODAL_KEY])
                      :size     :md
                      :backdrop :static}
            [:> ModalHeader "Add Spell to Spellbook"]
            [:> ModalBody {:class :modal-body}
             [:input {:id          :spell-search-text
                      :class       :spell-search
                      :value       spell-search-text
                      :placeholder "Search..."
                      :max-length  30
                      :on-change   #(dispatch [::events/spell-search-text (-> % .-target .-value)])}]
             [:hr]
             (into [:div.spell-list]
                   (for [{:keys [id name]} spells]
                        [:div
                         [:button.action-link {:on-click #(dispatch [::events/add-spell-to-spellbook id spellbook-id])}
                          name]]))]
            [:> ModalFooter {:class :modal-footer-buttons}
             [:button.action-link {:on-click #(dispatch [:toggle-modal SPELL_MODAL_KEY])}
              "Cancel"]]]))

(defn add-spell-button [spellbook-id]
      [:button.action-button {:on-click #(dispatch [::events/open-add-spell-modal spellbook-id])}
       "Add Spell"])

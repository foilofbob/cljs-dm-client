(ns cljs-dm-client.campaign-select.views
  (:require
   [cljs-dm-client.campaign-select.events :as events]
   [cljs-dm-client.campaign-select.subs :as subs]
   [cljs-dm-client.components.components :refer [logical-division]]
   [cljs-dm-client.components.forms :refer [text-input-row
                                            number-input-row
                                            select-input-row]]
   [cljs-dm-client.layout.views :refer [loading-wrapper]]
   [cljs-dm-client.navigation.views :refer [nav-button]]
   [re-frame.core :refer [dispatch
                          subscribe]]
   ["reactstrap/lib/Modal" :default Modal]
   ["reactstrap/lib/ModalBody" :default ModalBody]
   ["reactstrap/lib/ModalFooter" :default ModalFooter]
   ["reactstrap/lib/ModalHeader" :default ModalHeader]))

(def CAMPAIGN_MODAL_KEY :campaign-modal)

(defn campaign-modal []
  (let [campaign @(subscribe [:edit-object :edit-campaign])
        campaign-setting-options @(subscribe [::subs/campaign-settings-as-select-options])
        base {:obj campaign :obj-type "campaign"}]
    [:> Modal {:is-open @(subscribe [:modal-open? CAMPAIGN_MODAL_KEY])
               :toggle  #(dispatch [:toggle-modal CAMPAIGN_MODAL_KEY])
               :size    :md}
     [:> ModalHeader
      (if (:id campaign)
        "Update Campaign"
        "Add Campaign")]
     [:> ModalBody {:class :modal-body}
      [text-input-row "Campaign Name" 100 campaign "campaign" :name]
             ;; TODO: Weird number overflow???
      [number-input-row (merge base {:label "Current Player XP"
                                     :obj-key :current-player-xp})]
      [select-input-row "Campaign Setting" campaign "campaign" :campaign-setting-id campaign-setting-options]]
     [:> ModalFooter {:class :modal-footer-buttons}
      [:button.action-link {:on-click #(dispatch [:toggle-modal CAMPAIGN_MODAL_KEY])}
       "Cancel"]
      [:button.action-link {:on-click #(dispatch [::events/delete-campaign (:id campaign)])}
       "Delete"]
      [:button.action-button {:on-click #(dispatch [::events/edit-campaign campaign])}
       "Save"]]]))

(defn campaign-select []
  [loading-wrapper
   {:container [:div.select-campaign
                [logical-division {:text "Campaigns"}]]
    :content   [:<>
                [campaign-modal]
                (let [campaigns @(subscribe [::subs/campaigns])]
                  [:div.campaign-list

                   (if (seq campaigns)
                     (for [campaign campaigns]
                       [nav-button {:key     (:id campaign)
                                    :content (:name campaign)
                                    :handler [::events/select-campaign campaign]
                                    :target  :timeline}])
                     [:p "No campaigns!"])])
                [logical-division {:text [:button.action-link {:on-click #(dispatch [:toggle-modal CAMPAIGN_MODAL_KEY])}
                                          "Create New Campaign"]}]]}])

(ns cljs-dm-client.components.components
  (:require
   [ajax.core :as ajax]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cljs-dm-client.components.markdown :refer [render-markdown]]
   [cljs-dm-client.utils :as utils]
   [clojure.set :refer [rename-keys]]
   ["reactstrap/lib/Modal" :default Modal]
   ["reactstrap/lib/ModalBody" :default ModalBody]
   ["reactstrap/lib/ModalFooter" :default ModalFooter]
   ["reactstrap/lib/ModalHeader" :default ModalHeader]
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub subscribe]]))

(def NOTE_MODAL_KEY :note-modal)

(defn note-modal []
  (let [note @(subscribe [:edit-object :edit-note])]
    [:> Modal {:is-open @(subscribe [:modal-open? NOTE_MODAL_KEY])
               :toggle  #(dispatch [:toggle-modal NOTE_MODAL_KEY])
               :size    :xl}
     [:> ModalHeader
      (if (:id note)
        "Update Note"
        "Add Note")]
     [:> ModalBody {:class :modal-body-note}
      [:input {:value      (:title note)
               :max-length 100
               :on-change  #(dispatch [:update-edit-field :edit-note :title (-> % .-target .-value)])}]
      [:textarea {:value      (:content note)
                  :max-length 10000
                  :rows       20
                  :on-change  #(dispatch [:update-edit-field :edit-note :content (-> % .-target .-value)])}]]
     [:> ModalFooter {:class :modal-footer-buttons}
      [:button.action-link {:on-click #(dispatch [:toggle-modal NOTE_MODAL_KEY])}
       "Cancel"]
      [:button.action-link {:on-click #(dispatch [::delete-note (:id note)])}
       "Delete"]
      [:button.action-button {:on-click #(dispatch [::edit-note note])}
       "Save"]]]))

(defn note-for-campaign-object [camp-obj obj-type category]
      {:category       {:string category
                        :valid  true}
       :campaign-id    (:campaign-id camp-obj)
       :reference-type obj-type
       :reference-id   (:id camp-obj)})

(defn open-edit-note-modal [camp-obj obj-type category]
      (dispatch [:open-edit-note-modal camp-obj obj-type category]))

(reg-event-fx
 :open-edit-note-modal
 (fn [{:keys [db]} [_ camp-obj obj-type category]]
     {:fx [[:dispatch [:set-edit-object :edit-note (note-for-campaign-object camp-obj obj-type category)]]
           [:dispatch [:toggle-modal :note-modal]]]}))

(defn build-notes
      ([notes]
       (build-notes notes :notes-entry))
      ([notes class]
       (for [note notes]
            [:div {:key   (str "note-" (:id note))
                   :class class}
             (when (-> note :title seq)
                   [:div.note-title
                    [:strong (:title note)]])
             [:div.md-content
              {:dangerouslySetInnerHTML
               {:__html (render-markdown (:content note))}}]
             [:div.edit-container
              [:button.edit-button {:type     :button
                                    :on-click #(do (dispatch [:set-edit-object :edit-note note])
                                                   (dispatch [:toggle-modal :note-modal]))}]]])))

(reg-event-db
 :set-edit-object
 (fn [db [_ path object]]
   (assoc-in db [:page-data path] object)))

(reg-event-db
  :update-edit-field
  (fn [db [_ path field value]]
    (assoc-in db [:page-data path field] value)))

(reg-event-fx
  ::edit-note
  (fn [{:keys [db]} [_ note]]
      (let [new-note? (nil? (:id note))]
           {:db         (assoc db :action-status :working)
            :http-xhrio {:method          (if new-note? :post :put)
                         :uri             (cond-> (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/note")
                                                  (not new-note?)
                                                  (str "/" (:id note)))
                         :params          (rename-keys
                                            (cske/transform-keys csk/->PascalCase note)
                                            {:Id :ID :CampaignId :CampaignID :ReferenceId :ReferenceID})
                         :format          (ajax/json-request-format)
                         :response-format (ajax/json-response-format {:keywords? true})
                         :on-success      [::edit-note-success new-note?]
                         :on-failure      [:action-failure]}})))

(reg-event-fx
  ::edit-note-success
  (fn [{:keys [db]} [_ new-note? response]]
      (let [updated-note (utils/tranform-response response)]
        {:db (cond-> (assoc db :action-status :success)

                     new-note?
                     (update-in [:page-data :notes] concat [updated-note])

                     (not new-note?)
                     (update-in [:page-data :notes] 
                                #(map (fn [note]
                                          (if (= (:id note) (:id updated-note))
                                            updated-note
                                            note))
                                      %)))
         :fx [[:dispatch [:toggle-modal NOTE_MODAL_KEY]]]})))

(reg-event-fx
  ::delete-note
  (fn [{:keys [db]} [_ note-id]]
      {:db         (assoc db :action-status :working)
       :http-xhrio {:method          :delete
                    :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/note/" note-id)
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [::delete-note-success note-id]
                    :on-failure      [:action-failure]}}))

(reg-event-fx
  ::delete-note-success
  (fn [{:keys [db]} [_ note-id response]]
      {:db (-> db
               (assoc :action-status :success)
               (update-in [:page-data :notes] #(remove (fn [note] (= note-id (:id note))) %)))
       :fx [[:dispatch [:toggle-modal NOTE_MODAL_KEY]]]}))

(reg-sub
  :edit-object
  (fn [db [_ path]]
    (-> db :page-data path)))

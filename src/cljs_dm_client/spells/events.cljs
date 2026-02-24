(ns cljs-dm-client.spells.events
  (:require
   [ajax.core :as ajax]
   [cljs-dm-client.utils :as utils]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 :spells-page-load
 (utils/page-loader
  [:fetch-spells]
  [:fetch-spells-success]
  [:fetch-spells-failure]))

(reg-event-fx
 :fetch-spells
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             (str "http://localhost:8090/spells")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:fetch-spells-success]
                 :on-failure      [:fetch-spells-failure]}}))

(reg-event-db
 :fetch-spells-success
 (fn [db [_ response]]
   (utils/standard-success-handler db :spells response)))

(reg-event-fx
 :fetch-spells-failure
 utils/standard-failure-handler)

(reg-event-db
 ::spell-search-text
 (fn [db [_ input]]
   (assoc-in db [:page-data :spell-search-text] input)))

(reg-event-fx
 ::open-add-spell-modal
 (fn [{:keys [db]} [_ spellbook-id]]
   {:fx [[:dispatch [:set-edit-object :edit-spellbook-id spellbook-id]]
         [:dispatch [:toggle-modal :spell-modal]]]}))

(reg-event-fx
 ::add-spell-to-spellbook
 (fn [{:keys [db]} [_ spell-id spellbook-id]]
   {:db         (assoc db :action-status :working)
    :http-xhrio {:method          :post
                 :uri             (str "http://localhost:8090/campaign/"
                                       (-> db :selected-campaign :id)
                                       "/spellbook/"
                                       spellbook-id
                                       "/spell")
                 :params          {:SpellBookID spellbook-id
                                   :SpellID spell-id}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::add-spell-to-spellbook-success]
                 :on-failure      [:action-failure]}}))

(reg-event-db
 ::add-spell-to-spellbook-success
 (fn [db [_ response]]
   (let [new-entry (utils/tranform-response response)]
     (-> db
         (assoc :action-status :success)
         (update-in [:page-data :spellbooks]
                    #(map (fn [spellbook]
                            (if (= (:id spellbook) (:spell-book-id new-entry))
                              (update-in spellbook [:spell-book-entries] conj new-entry)
                              spellbook))
                          %))))))

(reg-event-fx
 ::remove-spellbook-entry
 (fn [{:keys [db]} [_ spellbook-entry-id spellbook-id]]
   {:db         (assoc db :action-status :working)
    :http-xhrio {:method          :delete
                 :uri             (str "http://localhost:8090/campaign/"
                                       (-> db :selected-campaign :id)
                                       "/spellbook/"
                                       spellbook-id
                                       "/spell/"
                                       spellbook-entry-id)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::remove-spellbook-entry-success spellbook-entry-id spellbook-id]
                 :on-failure      [:action-failure]}}))

(reg-event-db
 ::remove-spellbook-entry-success
 (fn [db [_ spellbook-entry-id spellbook-id response]]
   (-> db
       (assoc :action-status :success)
       (update-in [:page-data :spellbooks]
                  (fn [spellbooks]
                    (map (fn [spellbook]
                           (if (= (:id spellbook) spellbook-id)
                             (update-in spellbook [:spell-book-entries]
                                        #(remove (fn [entry] (= spellbook-entry-id (:id entry))) %))
                             spellbook))
                         spellbooks))))))

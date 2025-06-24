(ns cljs-dm-client.xp-tracker.events
  (:require
   [ajax.core :as ajax]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cljs-dm-client.utils :as utils]
   [clojure.set :refer [rename-keys]]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 :xp-tracker-page-load
 (utils/page-loader
  {:first-dispatch [::page-load-dispatcher]
   :rules [{:when :seen-all-of? :events [::fetch-experiences-success ::fetch-players-success :fetch-notes-success] :dispatch [:page-ready]}
           {:when :seen-any-of? :events [::fetch-experiences-failure ::fetch-players-failure :fetch-notes-failure] :halt? true}]}))

(reg-event-fx
 ::page-load-dispatcher
 (fn [_ _]
     {:dispatch-n [[::fetch-experiences]
                   [::fetch-players]
                   [:fetch-notes "experience"]]}))

(reg-event-fx
 ::fetch-experiences
 (fn [{:keys [db]} _]
     {:http-xhrio {:method          :get
                   :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/experiences")
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::fetch-experiences-success]
                   :on-failure      [::fetch-experiences-failure]}}))

(reg-event-db
 ::fetch-experiences-success
 (fn [db [_ response]]
     (utils/standard-success-handler db :experiences response)))

(reg-event-fx
 ::fetch-experiences-failure
 utils/standard-failure-handler)

(reg-event-fx
 ::fetch-players
 (fn [{:keys [db]} _]
     {:http-xhrio {:method          :get
                   :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/players")
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::fetch-players-success]
                   :on-failure      [::fetch-players-failure]}}))

(reg-event-db
 ::fetch-players-success
 (fn [db [_ response]]
     (utils/standard-success-handler db :players response)))

(reg-event-fx
 ::fetch-players-failure
 utils/standard-failure-handler)

(reg-event-fx
 ::open-edit-xp-modal
 (fn [{:keys [db]} [_ xp]]
     (let [edit-xp (or xp
                         {:campaign-id (-> db :selected-campaign :id)
                          :description ""
                          :xp 0
                          :finalized false})]
          {:fx [[:dispatch [:set-edit-object :edit-xp edit-xp]]
                [:dispatch [:toggle-modal :xp-modal]]]})))

(reg-event-db
 ::calculate-xp
 (fn [db [_]]
     (let [description (-> db :page-data :edit-xp :description)
           matches (re-seq #"(\d+)x CR([\d_]+)" description)
           computed (some->> matches
                             (map (fn [xp-set]
                                      (* (-> xp-set (nth 1) js/parseInt)
                                         (-> xp-set (nth 2) keyword utils/cr-xp-map))))
                             (apply +))]
          (when computed
                (assoc-in db [:page-data :edit-xp :xp] computed)))))

(reg-event-fx
 ::edit-xp
 (fn [{:keys [db]} [_ xp]]
     (let [new-xp? (nil? (:id xp))]
          {:db         (assoc db :action-status :working)
           :http-xhrio {:method          (if new-xp? :post :put)
                        :uri             (cond-> (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/experience")
                                                 (not new-xp?)
                                                 (str "/" (:id xp)))
                        :params          (update-in (rename-keys
                                                     (cske/transform-keys csk/->PascalCase xp)
                                                     {:Id :ID :CampaignId :CampaignID :Xp :XP})
                                                    [:XP]
                                                    #(some-> % js/parseInt))
                        :format          (ajax/json-request-format)
                        :response-format (ajax/json-response-format {:keywords? true})
                        :on-success      [::edit-xp-success new-xp?]
                        :on-failure      [:action-failure]}})))

(reg-event-fx
 ::edit-xp-success
 (fn [{:keys [db]} [_ new-xp? response]]
     (let [r (utils/tranform-response response)
           updated-xp (:experience r)
           campaign-xp (:campaign-xp r)
           campaign (-> db :selected-campaign (assoc :current-player-xp campaign-xp))]
          {:db (cond-> (assoc db :action-status :success)

                       true
                       (assoc-in [:selected-campaign :current-player-xp] campaign-xp)

                       new-xp?
                       (update-in [:page-data :experiences] concat [updated-xp])

                       (not new-xp?)
                       (update-in [:page-data :experiences]
                                  #(map (fn [xp]
                                            (if (= (:id xp) (:id updated-xp))
                                              updated-xp
                                              xp))
                                        %)))
           :update-in-session {:selected-campaign campaign}
           :fx [[:dispatch [:toggle-modal :xp-modal]]]})))

(reg-event-fx
 ::delete-xp
 (fn [{:keys [db]} [_ xp-id]]
     {:db         (assoc db :action-status :working)
      :http-xhrio {:method          :delete
                   :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/experience/" xp-id)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::delete-xp-success xp-id]
                   :on-failure      [:action-failure]}}))

(reg-event-fx
 ::delete-xp-success
 (fn [{:keys [db]} [_ xp-id response]]
     {:db (-> db
              (assoc :action-status :success)
              (update-in [:page-data :experiences] #(remove (fn [xp] (= xp-id (:id xp))) %)))
      :fx [[:dispatch [:toggle-modal :xp-modal]]]}))

(ns cljs-dm-client.timeline.events
  (:require
   [ajax.core :as ajax]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cljs-dm-client.utils :as utils]
   [clojure.set :refer [rename-keys]]
   [clojure.string :as string]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 :timeline-page-load
 (utils/page-loader
  [::page-load-dispatcher]
  [::fetch-game-days-success :fetch-notes-success]
  [::fetch-game-days-failure :fetch-notes-failure]))

(reg-event-fx
 ::page-load-dispatcher
 (fn [_ _]
   {:dispatch-n [[::fetch-game-days]
                 [:fetch-notes "game_day"]]}))

(reg-event-fx
 ::fetch-game-days
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/gameday")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::fetch-game-days-success]
                 :on-failure      [::fetch-game-days-failure]}}))

(reg-event-db
 ::fetch-game-days-success
 (fn [db [_ response]]
   (utils/standard-success-handler db :game-days response)))

(reg-event-fx
 ::fetch-game-days-failure
 utils/standard-failure-handler)

;; TODO: Wire in initial game post
(reg-event-fx
 ::post-game-day
 (fn [{:keys [db]} [_]]
   {:db         (assoc db :action-status :working)
    :http-xhrio {:method          :post
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/gameday")
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::post-game-day-success]
                 :on-failure      [:action-failure]}}))

(reg-event-fx
 ::post-game-day-success
 (fn [{:keys [db]} [_ response]]
   (let [new-game-day (utils/tranform-response response)]
     {:db (-> db
              (assoc :action-status :success)
              (update-in [:page-data :game-days] #(conj % new-game-day)))})))

;; TODO: Consolidate defs
(def GAME_DAY_MODAL_KEY :game-day-modal-key)

(reg-event-fx
 ::open-edit-game-day-modal
 (fn [{:keys [db]} [_ game-day]]
     (let [edit-game-day (or game-day
                             (reduce (fn [base cycle]
                                         (assoc base (->> cycle :id (str "cycle-") keyword) 1))
                                     {:campaign-id (-> db :selected-campaign :id)
                                      :in-game-day 1
                                      :day "1"
                                      :month "1"
                                      :year 1}
                                     (-> db :campaign-setting :calendar-cycles)))]
          {:fx [[:dispatch [:set-edit-object :edit-game-day edit-game-day]]
                [:dispatch [:toggle-modal GAME_DAY_MODAL_KEY]]]})))

(def game-day-keys #{:campaign-id :in-game-day :day :month :year})

(reg-event-fx
 ::initialize-game-day
 (fn [{:keys [db]} [_]]
     (let [edit-game-day (-> db :page-data :edit-game-day)
           game-day (-> edit-game-day
                        (select-keys game-day-keys)
                        (update-in [:day] js/parseInt)
                        (update-in [:month] js/parseInt))
           cycles (reduce (fn [cycles cycle-key]
                              (when-let [cycle-id (some-> cycle-key name (string/split #"-") last)]
                                        (conj cycles {:id (js/parseInt cycle-id) :offset (cycle-key edit-game-day)})))
                          []
                          (->> edit-game-day keys (remove game-day-keys)))]
          {:db         (assoc db :action-status :working)
           :http-xhrio {:method          :post
                        :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/gameday/initialize")
                        :params          {:GameDay (rename-keys
                                                    (cske/transform-keys csk/->PascalCase game-day)
                                                    {:CampaignId :CampaignID})
                                          :Cycles (mapv #(rename-keys
                                                          (cske/transform-keys csk/->PascalCase %)
                                                          {:Id :ID})
                                                        cycles)}
                        :format          (ajax/json-request-format)
                        :response-format (ajax/json-response-format {:keywords? true})
                        :on-success      [::post-game-day-success]
                        :on-failure      [:action-failure]}})))

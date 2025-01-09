(ns cljs-dm-client.layout.views
  (:require
   [cljs-dm-client.campaign-select.events :as campaign-events]
   [cljs-dm-client.navigation.views :refer [nav-button]]
   [re-frame.core :refer [dispatch subscribe] :as re-frame]))

(def page-mappings
  [{:id      :timeline
    :name    "Timeline / Events"}
   {:id      :party
    :name    "Party Members"}
   {:id      :locations
    :name    "Locations"}])

(defn header-content []
  (let [campaign @(subscribe [:selected-campaign])
        current-panel @(subscribe [:active-panel-key])]
    [:div.header-content
     [:div.campaign
      [:h2.subtitle (:name campaign)]
      [nav-button {:content "Back to Campaign Select"
                   :handler [::campaign-events/select-campaign nil]
                   :target  :campaign-select
                   :link?   true}]]
     [:h1.current-panel
      (->> page-mappings
        (filter #(= current-panel (:id %)))
        first
        :name)]]))

(defn left-nav []
  (into [:div.left-nav]
    (for [{id :id name :name handler :handler} page-mappings]
      [nav-button {:key     id
                   :content name
                   :handler handler
                   :target  id}])))

;; TODO: Paceholder for now, not sure what I want here (but helps with layout)
(defn right-panel []
  [:div.right-panel])

(defn campaign-panel [& panel-content]
  [:div.campaign-manager
   [header-content]
   [:div.campaign-body-content
    [left-nav]
    (into [:div.panel-content]
      panel-content)
    [right-panel]
    ]])

(def loading-spinner [:div.loader])

(defn page-error []
  (let [error-str @(subscribe [:page-error])]
    [:div.error error-str]))

(defn loading-wrapper [{:keys [loading-handler container content]}]
  (let [loading-status @(subscribe [:loading-status])]
    (into container
      [(case loading-status
         :success content
         :failure page-error
         (do
           (dispatch loading-handler)
           loading-spinner))])))

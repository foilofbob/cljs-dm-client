(ns cljs-dm-client.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [day8.re-frame.http-fx]
   [cljs-dm-client.config :as config]
   [cljs-dm-client.events :as events]
   [cljs-dm-client.routes :as routes]
   [cljs-dm-client.subs]
   [cljs-dm-client.views :as views]
   [cljs-dm-client.layout.events]
   [cljs-dm-client.layout.views]
   [cljs-dm-client.layout.subs]
   [cljs-dm-client.locations.events]
   [cljs-dm-client.locations.views]
   [cljs-dm-client.locations.subs]
   [cljs-dm-client.navigation.events :as nav]
   [cljs-dm-client.navigation.subs]
   [cljs-dm-client.navigation.views]
   [cljs-dm-client.party.events]
   [cljs-dm-client.party.views]
   [cljs-dm-client.party.subs]
   [cljs-dm-client.timeline.events]
   [cljs-dm-client.timeline.views]
   [cljs-dm-client.timeline.subs]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)
       (re-frame/dispatch [::nav/navigate :campaign-select])))

(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))

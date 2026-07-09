(ns cljs-dm-client.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [day8.re-frame.http-fx]
   [day8.re-frame.async-flow-fx :as async-flow-fx]
   [cljs-dm-client.config :as config]
   [cljs-dm-client.events :as events]
   [cljs-dm-client.routes :as routes]
   [cljs-dm-client.subs]
   [cljs-dm-client.views :as views]
   [cljs-dm-client.layout.events]
   [cljs-dm-client.layout.views]
   [cljs-dm-client.layout.subs]
   [cljs-dm-client.locations.views]
   [cljs-dm-client.miscellaneous.views]
   [cljs-dm-client.navigation.subs]
   [cljs-dm-client.navigation.views]
   [cljs-dm-client.characters.party.events]
   [cljs-dm-client.characters.party.views]
   [cljs-dm-client.characters.npcs.events]
   [cljs-dm-client.characters.npcs.views]
   [cljs-dm-client.player-stories.events]
   [cljs-dm-client.player-stories.views]
   [cljs-dm-client.timeline.views]
   [cljs-dm-client.xp-tracker.views]
   [cljs-dm-client.login.views]
   [cljs-dm-client.login.events]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)
       ;; TODO: This needs to move to the post-auth handler
    (re-frame/dispatch [:resume-session-if-present])))

(defn- add-credentials [request-map]
  (if (map? request-map)
    (assoc request-map :with-credentials true)
    request-map))

(def with-global-credentials
  (re-frame/->interceptor
   :id      :with-global-credentials
   :after   (fn [context]
              (let [effects (re-frame/get-effect context)]
                (if-let [http-fx (:http-xhrio effects)]
                  (let [updated-http (if (vector? http-fx)
                                       (mapv add-credentials http-fx)
                                       (add-credentials http-fx))]
                    (re-frame/assoc-effect context :http-xhrio updated-http))
                  context)))))

(def check-auth
  (re-frame/->interceptor
   :id      :check-auth
   :before  (fn [context]
              (let [db             (get-in context [:coeffects :db])
                    authenticated? (get-in db [:auth :authenticated?])]
                (js/console.error "Not authenticated - redirecting to login")
                (if authenticated?
                  context
                  (-> context
                      (re-frame/enqueue [])
                      (assoc-in [:effects :dispatch] [:navigate :login])))))))

(defn init []
  (re-frame/reg-global-interceptor with-global-credentials)
  (routes/start!)
  (dev-setup)
  (re-frame/dispatch-sync [::events/initialize-db])
  (mount-root))

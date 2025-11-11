(ns cljs-dm-client.utils
  (:require
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cognitect.transit :as t]))

(defn page-loader
      ([dispatcher-fn page-ready-events page-error-events]
       (page-loader dispatcher-fn page-ready-events page-error-events true))
      ([dispatcher-fn page-ready-events page-error-events force-campaign-select?]
       (fn [{:keys [db]} _]
           (if (and (-> db :selected-campaign nil?) force-campaign-select?)
             {:navigate :campaign-select}
             {:db         (assoc db :loading-status :loading)
              :async-flow {:first-dispatch dispatcher-fn
                           :rules [{:when :seen-all-of? :events page-ready-events :dispatch [:page-ready]}
                                   {:when :seen-any-of? :events page-error-events :halt? true}]}}))))

(defn write-to-session
  "Expecting to write a map containing active-panel and selected-campaign"
  [session-data]
  (try
    (js/sessionStorage.setItem "campaign-manager-session" (js/JSON.stringify (clj->js session-data)))
    (catch :default e
      (print "Exception writing session data: " e))))

(defn read-from-session []
  (try
    (or (some-> (.getItem js/sessionStorage "campaign-manager-session")
                (js/JSON.parse)
                (js->clj :keywordize-keys true))
        {})
    (catch :default e
      (print "Exception reading from session data: " e)
      {})))

(defn update-in-session [update-map]
  (let [current-session (read-from-session)
        updated-session (if (seq current-session)
                          (merge current-session update-map)
                          update-map)]
    (write-to-session updated-session)))

(defn tranform-response
  "Takes a response body and updates keys to be kebab-case-keyword"
  [response]
  (cske/transform-keys csk/->kebab-case-keyword response))

(defn standard-success-handler
  "This will replace existing data"
  [db path response]
  (assoc-in db [:page-data path] (tranform-response response)))

(defn append-success-handler
  "This will add onto existing data, expecting to joining lists"
  [db path response]
  (update-in db [:page-data path] #(concat (or % []) (tranform-response response))))

(defn standard-failure-handler [db [_ response]]
  (-> db
      (assoc :loading-status :failure)
      (assoc :page-error response)))

(def character-levels
  [{:lvl 1 :xp 0 :proficiency 2}
   {:lvl 2 :xp 300 :proficiency 2}
   {:lvl 3 :xp 900 :proficiency 2}
   {:lvl 4 :xp 2700 :proficiency 2}
   {:lvl 5 :xp 6500 :proficiency 3}
   {:lvl 6 :xp 14000 :proficiency 3}
   {:lvl 7 :xp 23000 :proficiency 3}
   {:lvl 8 :xp 34000 :proficiency 3}
   {:lvl 9 :xp 48000 :proficiency 4}
   {:lvl 10 :xp 64000 :proficiency 4}
   {:lvl 11 :xp 85000 :proficiency 4}
   {:lvl 12 :xp 100000 :proficiency 4}
   {:lvl 13 :xp 120000 :proficiency 5}
   {:lvl 14 :xp 140000 :proficiency 5}
   {:lvl 15 :xp 165000 :proficiency 5}
   {:lvl 16 :xp 195000 :proficiency 5}
   {:lvl 17 :xp 225000 :proficiency 6}
   {:lvl 18 :xp 265000 :proficiency 6}
   {:lvl 19 :xp 305000 :proficiency 6}
   {:lvl 20 :xp 355000 :proficiency 6}])

(defn level-by-xp [xp]
  (->> character-levels
       (filter #(<= (:xp %) xp))
       last))

(defn next-level-by-xp [xp]
  (->> character-levels
       (filter #(> (:xp %) xp))
       first))

(defn level-by-level [lvl]
      (->> character-levels
           (filter #(= lvl (:lvl %)))
           first))

(defn percentage [numerator denominator]
  (-> numerator (/ denominator) (* 100) Math/floor))

;; TODO: Not sure if I'll end up needing this
(def cr-xp
  [{:cr 0 :xp 10}
   {:cr 0.125 :xp 25}
   {:cr 0.25 :xp 50}
   {:cr 0.5 :xp 100}
   {:cr 1 :xp 200}
   {:cr 2 :xp 450}
   {:cr 3 :xp 700}
   {:cr 4 :xp 1100}
   {:cr 5 :xp 1800}
   {:cr 6 :xp 2300}
   {:cr 7 :xp 2900}
   {:cr 8 :xp 3900}
   {:cr 9 :xp 5000}
   {:cr 10 :xp 5900}
   {:cr 11 :xp 7200}
   {:cr 12 :xp 8400}
   {:cr 13 :xp 10000}
   {:cr 14 :xp 11500}
   {:cr 15 :xp 13000}
   {:cr 16 :xp 15000}
   {:cr 17 :xp 18000}
   {:cr 18 :xp 20000}
   {:cr 19 :xp 22000}
   {:cr 20 :xp 25000}
   {:cr 21 :xp 33000}
   {:cr 22 :xp 41000}
   {:cr 23 :xp 50000}
   {:cr 24 :xp 60000}])

(def cr-xp-map
  {:0 10
   :1_8 25
   :1_4 50
   :1_2 100
   :1 200
   :2 450
   :3 700
   :4 1100
   :5 1800
   :6 2300
   :7 2900
   :8 3900
   :9 5000
   :10 5900
   :11 7200
   :12 8400
   :13 10000
   :14 11500
   :15 13000
   :16 15000
   :17 18000
   :18 20000
   :19 22000
   :20 25000
   :21 33000
   :22 41000
   :23 50000
   :24 60000})

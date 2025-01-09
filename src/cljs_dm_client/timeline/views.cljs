(ns cljs-dm-client.timeline.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [cljs-dm-client.timeline.events :as events]
    [cljs-dm-client.timeline.subs :as subs]
    [cljs-dm-client.layout.views :refer [campaign-panel
                                         loading-wrapper]]
    [markdown.core :refer [md->html]]))

(defn game-day-card [game-day notes]
  (let [play-sessions (filter #(-> % :category :string seq not) notes)
        special-notes (filter #(-> % :category :string seq) notes)]
    [:div.card.game-day
     [:div.game-day-tracker
      [:div (str "Month-" (:month game-day) " " (:day game-day) ", " (:year game-day))]
      [:div (str "In game day " (:in-game-day game-day))]
      [:div "Catha: TODO"]
      [:div "Ruidus: TODO"]
      [:div "Holidays: TODO"]]
     (into [:div.game-day-events]
       (for [ps play-sessions]
         [:<>
          [:div.note-title
           [:strong (:title ps)]]
          [:div.md-content {:dangerouslySetInnerHTML {:__html (md->html (:content ps))}}]
          [:div.edit-container
           [:button.edit-button {:type :button}]]]))
     (into [:div.game-day-notes]
       (for [sn special-notes]
         [:<>
          [:div {:class (-> sn :category :string)}
           (:title sn)]
          [:div (:content sn)]
          ]))
     ]))

(defn timeline-content []
  (let [game-days @(subscribe [::subs/game-days])
        notes @(subscribe [:notes])]
    (into [:<>]
      (for [gd game-days]
        [game-day-card gd (filter #(= (:id gd) (:reference-id %)) notes)]))
    ))

(defn timeline []
  [loading-wrapper
   {:loading-handler [::events/timeline-page-load]
    :container       [campaign-panel]
    :content         [timeline-content]}])
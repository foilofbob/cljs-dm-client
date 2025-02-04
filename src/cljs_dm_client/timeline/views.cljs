(ns cljs-dm-client.timeline.views
  (:require
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-dm-client.components.components :refer [note-modal]]
   [cljs-dm-client.layout.views :refer [campaign-panel
                                        loading-wrapper]]
   [cljs-dm-client.timeline.events :as events]
   [cljs-dm-client.timeline.subs :as subs]
   [markdown.core :refer [md->html]]))

(defn new-note-for-game-day [game-day category]
      {:category       {:string category
                        :valid  true}
       :campaign-id    (:campaign-id game-day)
       :reference-type "GAME_DAY"
       :reference-id   (:id game-day)})

(defn build-notes [notes]
      (for [note notes]
           [:div.events-entry
            (when (-> note :title seq)
                  [:div.note-title
                   [:strong (:title note)]])
            [:div.md-content {:dangerouslySetInnerHTML {:__html (md->html (:content note))}}]
            [:div.edit-container
             [:button.edit-button {:type     :button
                                   :on-click #(do (dispatch [:set-edit-object :edit-note note])
                                                  (dispatch [:toggle-modal :note-modal]))}]]]))

(defn open-edit-modal [game-day category]
      (do (dispatch [:set-edit-object :edit-note (new-note-for-game-day game-day category)])
          (dispatch [:toggle-modal :note-modal])))

(defn month-label [months month-num]
      (->> months
           (filter #(= month-num (:order %)))
           first
           :name))

(defn day-of-week [week-days in-game-day]
      (let [day-num (-> in-game-day
                        (+ 3) ;; TODO: This offset will eventually be data driven!!!!
                        (- 1) ;; Because IGD is 1+, mod is 0 based
                        (mod 7)
                        (+ 1))]
           (->> week-days
                (filter #(= day-num (:order %)))
                first
                :name)))

(defn game-day-card [game-day notes]
      (let [campaign-setting @(subscribe [:campaign-setting])
            play-sessions    (filter #(-> % :category :string seq not) notes)
            special-notes    (filter #(-> % :category :string seq) notes)]
           [:div.common-panel
            [:div.game-day
             [:div.game-day-tracker
              [:div (str (month-label (:months campaign-setting) (:month game-day)) " " (:day game-day) ", " (:year game-day))]
              [:div (str (day-of-week (:week-days campaign-setting) (:in-game-day game-day)))]
              ;[:div "Catha: TODO"]
              ;[:div "Ruidus: TODO"]
              ;[:div "Holidays: TODO"]
              ]
             (into [:div.game-day-events]
                   (build-notes play-sessions))

             (into [:div.game-day-notes]
                   (build-notes special-notes))]
            [:div.game-day-controls
             [:button.action-link {:on-click #(open-edit-modal game-day "")}
              "Add Events"]
             [:button.action-link {:on-click #(open-edit-modal game-day "note")}
              "Add Notes"]]]))

(defn timeline-content []
      (let [game-days @(subscribe [::subs/game-days])
            notes     @(subscribe [:notes])]
           [:<>
            [:div
             [:button.action-button {:on-click #(dispatch [::events/post-game-day])}
              "Add Game Day"]]
            (for [gd game-days] ^{:key (str "game-day-" (:id gd))}
                 [game-day-card gd (filter #(= (:id gd) (:reference-id %)) notes)])]))

(defn timeline []
      [:<>
       [note-modal]
       [loading-wrapper
        {:loading-handler [::events/timeline-page-load]
         :container       [campaign-panel]
         :content         [timeline-content]}]])
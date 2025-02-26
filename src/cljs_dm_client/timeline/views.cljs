(ns cljs-dm-client.timeline.views
  (:require
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-dm-client.components.components :refer [note-modal]]
   [cljs-dm-client.layout.views :refer [campaign-panel
                                        loading-wrapper]]
   [cljs-dm-client.timeline.events :as events]
   [cljs-dm-client.timeline.subs :as subs]
   ["markdown-it" :as md]
   ["markdown-it-admon" :as mda]
   ["markdown-it-task-lists" :as mdts]
   [reagent.core :as reagent]
   ["reactstrap/lib/UncontrolledTooltip" :default UncontrolledTooltip]))

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
            [:div.md-content
             {:dangerouslySetInnerHTML
              {:__html
               (-> (md)
                   (.use mda)
                   (.use mdts)
                   (.render (:content note)))}}]
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

(defn day-of-week-int [in-game-day offset]
      (-> in-game-day
          (+ offset)
          (- 1) ;; Because IGD is 1+, mod is 0 based
          (mod 7)
          (+ 1)))

(defn day-of-week [week-days in-game-day]
      (let [day-num (day-of-week-int in-game-day 3)]
           (->> week-days
                (filter #(= day-num (:order %)))
                first
                :name)))

(defn cycle-int [in-game-day period offset]
      (-> in-game-day
          (+ offset)
          (- 1) ;; Because IGD is 1+, mod is 0 based
          (mod period)
          (+ 1)))

(defn cycle-status [cycle in-game-day offset]
      (let [current (cycle-int in-game-day (:period cycle) offset)]
           (str (:name cycle) " " current "/" (:period cycle))))

(defn game-day-card [game-day notes]
      (let [campaign-setting @(subscribe [:campaign-setting])
            play-sessions    (filter #(-> % :category :string seq not) notes)
            special-notes    (filter #(-> % :category :string seq) notes)
            game-day-id      (str "game-day-" (:id game-day))]
           [:div.common-panel
            [:div.game-day
             [:div.game-day-tracker
              [:div {:id game-day-id}
               (str (month-label (:months campaign-setting) (:month game-day)) " " (:day game-day))]
              [:div (str (day-of-week (:week-days campaign-setting) (:in-game-day game-day)))]
              (when-let [holiday (some->> campaign-setting
                                          :calendar-events
                                          (filter #(and (= (:month game-day) (:month %))
                                                        (= (:day game-day) (:day %))))
                                          first)]
                        (let [holiday-id (str "holiday-" (:id holiday))]
                             [:<>
                              [:div.holiday {:id holiday-id}
                               (:name holiday)]
                              [:> UncontrolledTooltip {:target           holiday-id
                                                       :placement        "right"
                                                       :inner-class-name "component-tooltip"}
                               [:div (:description holiday)]]]))]
             (into [:div.game-day-events]
                   (build-notes play-sessions))
             (into [:div.game-day-notes]
                   (build-notes special-notes))]
            [:div.game-day-controls
             [:button.action-link {:on-click #(open-edit-modal game-day "")}
              "Add Events"]
             [:button.action-link {:on-click #(open-edit-modal game-day "note")}
              "Add Notes"]]
            [:> UncontrolledTooltip {:target           game-day-id
                                     :placement        "top"
                                     :inner-class-name "component-tooltip"}
             [:div "In game day " (:in-game-day game-day)]
             [:div "Year " (:year game-day)]
             (into [:<>]
                   (for [cycle (:calendar-cycles campaign-setting)]
                        (let [offset (or (some->> campaign-setting
                                                  :calendar-cycle-offsets
                                                  (filter #(= (:id cycle) (:calendar-cycle-id %)))
                                                  first
                                                  :offset)
                                         0)]
                             [:div (cycle-status cycle (:in-game-day game-day) offset)])))]]))

(defn timeline-content []
      (let [game-days @(subscribe [::subs/game-days])
            notes     @(subscribe [:notes])]
           [:<>
            [:div.panel-content
             [:div
              [:button.action-button {:on-click #(dispatch [::events/post-game-day])}
               "Add Game Day"]]
             (for [gd game-days] ^{:key (str "game-day-" (:id gd))}
                  [game-day-card gd (filter #(= (:id gd) (:reference-id %)) notes)])]
            [:div.right-panel
             [:button.action-button {:on-click #(open-edit-modal {:campaign-id 1} "global")}
              "Add Global Note"]

             (for [note (filter #(= "global" (some-> % :category :string)) notes)]
                  [:div.common-panel {:key (str "global-" (:id note))}
                   [:div.md-content
                    {:dangerouslySetInnerHTML
                     {:__html
                      (-> (md)
                          (.use mda)
                          (.render (:content note)))}}]
                   [:div.edit-container
                    [:button.edit-button {:type     :button
                                          :on-click #(do (dispatch [:set-edit-object :edit-note note])
                                                         (dispatch [:toggle-modal :note-modal]))}]]])]]))

(defn timeline []
      [:<>
       [note-modal]
       [loading-wrapper
        {:loading-handler [::events/timeline-page-load]
         :container       [campaign-panel]
         :content         [timeline-content]}]])
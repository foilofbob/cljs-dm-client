(ns cljs-dm-client.timeline.views
  (:require
   [clojure.math :as math]
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-dm-client.components.forms :refer [build-options
                                            number-input-row
                                            select-input-row]]
   [cljs-dm-client.components.notes :refer [build-notes
                                            note-modal]]
   [cljs-dm-client.layout.views :refer [campaign-panel
                                        loading-wrapper]]
   [cljs-dm-client.timeline.events :as events]
   [cljs-dm-client.timeline.subs :as subs]
   ["reactstrap/lib/Modal" :default Modal]
   ["reactstrap/lib/ModalBody" :default ModalBody]
   ["reactstrap/lib/ModalFooter" :default ModalFooter]
   ["reactstrap/lib/ModalHeader" :default ModalHeader]
   ["reactstrap/lib/UncontrolledTooltip" :default UncontrolledTooltip]))

(def GAME_DAY_MODAL_KEY :game-day-modal-key)

;; Currently just meant for adding initial day
(defn game-day-modal []
  (let [game-day @(subscribe [:edit-object :edit-game-day])
        campaign-setting @(subscribe [::subs/campaign-setting])
        {:keys [months week-days calendar-cycles]} campaign-setting
        month-options (build-options {:objects months
                                      :blank? false
                                      :sort :order})
        selected-month (->> months (filter #(= (:month game-day) (str (:id %)))) first)
        day-options (map (fn [n] {:value n :label n})
                         (range 1 (-> selected-month :num-days js/parseInt (+ 1))))
        base {:obj game-day :obj-type "game-day"}]
    [:> Modal {:is-open @(subscribe [:modal-open? GAME_DAY_MODAL_KEY])
               :toggle  #(dispatch [:toggle-modal GAME_DAY_MODAL_KEY])
               :size    :sm}
     [:> ModalHeader "Starting Game Day"]
     [:> ModalBody {:class :modal-body}
      [number-input-row (merge base {:label "In Game Day"
                                     :max 9999
                                     :obj-key :in-game-day})]
      [select-input-row "Day" game-day "game-day" :day day-options]
      [select-input-row (merge base {:label "Month"
                                     :obj-key :month
                                     :options month-options
                                     :after-change-handler [:update-edit-field :edit-game-day :day 1]})]
      [number-input-row (merge base {:label "Year"
                                     :max 99999
                                     :obj-key :year})]

      (into [:<>]
            (for [cycle calendar-cycles]
              [number-input-row (merge base {:label (:name cycle)
                                             :min 1
                                             :max (:period cycle)
                                             :obj-key (->> cycle :id (str "cycle-") keyword)})]))]
     [:> ModalFooter {:class :modal-footer-buttons}
      [:button.action-link {:on-click #(dispatch [:toggle-modal GAME_DAY_MODAL_KEY])}
       "Cancel"]
      [:button.action-button {:on-click #(dispatch [::events/initialize-game-day])}
       "Save"]]]))

(defn open-edit-modal [game-day category]
  (dispatch [:open-edit-note-modal game-day "GAME_DAY" category]))

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

;; TODO: the offset here is fixed, needs to be dynamic. Where to track?
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

(defn cycle-elems
  ([game-day campaign-setting]
   (cycle-elems game-day campaign-setting true))
  ([game-day campaign-setting include-all?]
   (into [:<>]
         (for [{period :period cycle-name :name cycle-id :id} (:calendar-cycles campaign-setting)]
           (let [offset (or (some->> campaign-setting
                                     :calendar-cycle-offsets
                                     (filter #(= cycle-id (:calendar-cycle-id %)))
                                     first
                                     :offset)
                            0)
                 current (cycle-int (:in-game-day game-day) period offset)]
             (when (or include-all? (= current period) (= current (math/round (/ period 2))))
               [:div.day-highlight (str cycle-name " " current "/" period)]))))))

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
            [:div.day-highlight {:id holiday-id}
             (:name holiday)]
            [:> UncontrolledTooltip {:target           holiday-id
                                     :placement        "right"
                                     :inner-class-name "component-tooltip"}
             [:div (:description holiday)]]]))
       [cycle-elems game-day campaign-setting false]]
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
      [cycle-elems game-day campaign-setting]]]))

(defn timeline-content []
  (let [game-days @(subscribe [::subs/game-days])
        notes     @(subscribe [:notes])]
    [:<>
     [:div.panel-content
      [:div
       [:button.action-button {:on-click (if (seq game-days)
                                           #(dispatch [::events/post-game-day])
                                           #(dispatch [::events/open-edit-game-day-modal]))}
        "Add Game Day"]]
      (for [game-day game-days] ^{:key (str "game-day-" (:id game-day))}
           [game-day-card game-day (filter #(= (:id game-day) (:reference-id %)) notes)])]
     [:div.right-panel
      [:button.action-button {:on-click #(open-edit-modal {:campaign-id 1} "global")}
       "Add Global Note"]
      (build-notes (filter #(= "global" (some-> % :category :string)) notes) [:common-panel :notes-entry])]]))

(defn timeline []
  [:<>
   [note-modal]
   [game-day-modal]
   [loading-wrapper
    {:container [campaign-panel]
     :content   [timeline-content]}]])
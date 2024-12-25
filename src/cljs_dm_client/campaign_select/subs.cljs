(ns cljs-dm-client.campaign-select.subs
  (:require
    [re-frame.core :refer [reg-sub]]))

(reg-sub
  ::campaigns
  (fn [db _]
      [{:id 1
        :name "Adventures in Wonderland"}
       {:id 2
        :name "M.O.U.S.E."}
       {:id 3
        :name "Happy Harpoon"}]))


(ns cljs-dm-client.new-view.views
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-dm-client.layout.views :refer [campaign-panel
                                        loading-wrapper]]
   [cljs-dm-client.new-view.events :as events]
   ;[cljs-dm-client.new-view.subs :as subs]
   ["reactstrap/lib/Modal" :default Modal]
   ["reactstrap/lib/ModalBody" :default ModalBody]
   ["reactstrap/lib/ModalFooter" :default ModalFooter]
   ["reactstrap/lib/ModalHeader" :default ModalHeader]))


(defn new-view []
      [loading-wrapper
       {:container [campaign-panel]
        :content   [:h1 "AAAAAAAAAA"]}])
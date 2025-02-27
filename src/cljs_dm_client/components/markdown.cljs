(ns cljs-dm-client.components.markdown
  (:require
   ["markdown-it" :as md]
   ["markdown-it-admon" :as mda]
   ["markdown-it-task-lists" :as mdts]))

(defonce markdown
         (-> (md)
             (.use mda)
             (.use mdts #js {:enabled true})))

(defn render-markdown [content]
      (.render markdown content))
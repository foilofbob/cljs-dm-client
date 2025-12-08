(ns cljs-dm-client.components.markdown
  (:require
   ["markdown-it" :as md]
   ["markdown-it-admon" :as mda]
   ["markdown-it-task-lists" :as mdts]
   ["markdown-it-link-attributes" :as mdla]))

(defonce markdown
  (-> (md)
      (.use mda)
      (.use mdts #js {:enabled true})
      ;; TODO: link attributes doesn't seem to work...
      (.use mdla #js {:attrs {:target "_blank" :rel "noopener"}})))

(defn render-markdown [content]
  (.render markdown content))

(defn markdown-div [content]
  [:div.md-content
   {:dangerouslySetInnerHTML
    {:__html (render-markdown content)}}])

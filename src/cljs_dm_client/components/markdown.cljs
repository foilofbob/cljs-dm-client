(ns cljs-dm-client.components.markdown
  (:require
   ["markdown-it" :as md]
   ["markdown-it-admon" :as mda]
   ["markdown-it-classy" :as mdclassy]
   ["markdown-it-collapsible" :as mdcollapse]
   ["markdown-it-task-lists" :as mdts]
   ["markdown-it-link-attributes" :as mdla]
   ["markdown-it-image-figures" :as mdif]
   #_["markdown-it-plantuml" :as plantuml]
   #_["markdown-it-regexp" :as regexp]))

(defonce markdown
  (-> (md)
      (.use mda)
      (.use mdts #js {:enabled true})

      ;; link attributes doesn't seem to work...
      ;(.use mdla #js {:attrs {:target "_blank" :rel "noopener"}})

      ;; Could be useful? Do we have need for a UML diagrams?
      ;; https://plantuml.com/class-diagram
      ;(.use plantuml)

      ;; Could be useful for custom matchers? Perhaps dynamic links? There are custom options, see docs.
      ;; Also seems to have a broken dependency...
      ;; https://www.npmjs.com/package/markdown-it-regexp
      ;;(.use regexp)

      ;; Could be useful for custom tag classes
      ;; https://www.npmjs.com/package/markdown-it-classy
      (.use mdclassy)

      ;; Collapsible components
      ;; https://www.npmjs.com/package/markdown-it-collapsible
      (.use mdcollapse)

      (.use mdif #js {:lazy true :async true}))) ;; ex.: [![](fig.png)](page.html)

(defn render-markdown [content]
  (.render markdown content))

(defn markdown-div [content]
  [:div.md-content
   {:dangerouslySetInnerHTML
    {:__html (render-markdown content)}}])

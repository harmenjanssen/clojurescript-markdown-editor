(ns markdownify.main
  (:require [reagent.core :as reagent]
            ["showdown" :as showdown]))

(defonce showdown-converter (showdown/Converter.))

(defonce flash-message-timeout (reagent/atom nil))
(defonce flash-message (reagent/atom nil))
(defonce text-state (reagent/atom {:format :md
                                   :value ""}))

(defn flash
  ([text]
   (flash text 3000))
  ([text ms]
   (js/clearTimeout @flash-message-timeout)
   (reset! flash-message text)
   (reset! flash-message-timeout
           (js/setTimeout #(reset! flash-message nil) ms))))

(defn md->html [md]
  (.makeHtml showdown-converter md))

(defn html->md [html]
  (.makeMarkdown showdown-converter html))

(defn ->md [{:keys [format value]}]
  (case format
    :md value
    :html (html->md value)))

(defn ->html [{:keys [format value]}]
  (case format
    :md (md->html value)
    :html value))

(defn copy-to-clipboard [s]
  (let [el (.createElement js/document "textarea")
        selected (when (pos? (-> js/document .getSelection .-rangeCount))
                   (-> js/document .getSelection (.getRangeAt 0)))]
    (set! (.-value el) s)
    (.setAttribute el "readonly" "")
    (set! (-> el .-style .-position) "absolute")
    (set! (-> el .-style .-left) "-9999px")
    (-> js/document .-body (.appendChild el))
    (.select el)
    (.execCommand js/document "copy")
    (-> js/document .-body (.removeChild el))
    (when selected
      (-> js/document .getSelection .removeAllRanges)
      (-> js/document .getSelection (.addRange selected)))))

(defn app []
  [:div
    [:div
     {:style {:position :fixed
              :top 0
              :left "50%"
              :max-width 250
              :padding "1em"
              :text-align :center
              :background :yellow
              :transform (if @flash-message
                           "scaleY(1) translateX(-50%)"
                           "scaleY(0) translateX(-50%)")
              :transform-origin :top
              :transition "transform 100ms ease-in"
              :z-index 100}}
     @flash-message]
    [:h1 "Markdownify"]
    [:div
      {:style {:display :flex}}
      [:div
        {:style {:flex "1"}}
        [:h2 "Markdown"]
        [:textarea
          {:on-change (fn [e]
                        (reset! text-state {:format :md :value (-> e .-target .-value)}))
            :value (->md @text-state)
            :style {:resize "none"
                    :height "500px"
                    :width "100%"}}]
        [:button
         {:on-click (fn []
                      (copy-to-clipboard (->md @text-state))
                      (flash "Markdown copied to clipboard!"))
          :style {:background-color :green
                  :padding "1em"
                  :color :white
                  :border-radius 10}}
         "Copy markdown"]]

      [:div
        {:style {:flex "1"}}
        [:h2 "HTML"]
        [:textarea
          {:on-change (fn [e]
                        (reset! text-state {:format :html :value (-> e .-target .-value)}))
            :value (->html @text-state)
            :style {:resize "none"
                    :height "500px"
                    :width "100%"}}]
        [:button
         {:on-click (fn []
                      (copy-to-clipboard (->html @text-state))
                      (flash "HTML copied to clipboard!"))
          :style {:background-color :green
                  :padding "1em"
                  :color :white
                  :border-radius 10}}
         "Copy HTML"]]

      [:div
        {:style {:flex "1" :padding-left "2em"}}
        [:h2 "HTML Preview"]
        [:div {:style {:height "500px"}
               :dangerouslySetInnerHTML {:__html (->html @text-state)}}]
        ]
    ]])

(defn mount! []
  (reagent/render [app]
                  (.getElementById js/document "app")))

(defn main! []
  (println "Welcome to the app!")
  (mount!))

(defn reload! []
  (println "Reloaded!")
  (mount!))

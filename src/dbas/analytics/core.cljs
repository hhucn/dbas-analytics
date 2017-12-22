(ns dbas.analytics.core
  (:require-macros)
  (:require [reagent.core :as r :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [dbas.analytics.charts :refer [statements-of-issues-chart arguments-of-issues]]
            [dbas.analytics.utils :refer [time-ago seconds minutes hours days]]))

;; -------------------------
;; Views


(defn home-page []
  [:div
   [:div.row.my-2
    [:div.col-md-6 [statements-of-issues-chart {:refresh-timeout (seconds 60)}]]
    [:div.col-md-6 [arguments-of-issues {:refresh-timeout (seconds 60)}]]]])

(defn about-page []
  [:div.row
   [:p "This will be an about page!"]])

;; -------------------------
;; Routes

(def page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
                    (reset! page #'home-page))

(secretary/defroute "/about" []
                    (reset! page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))

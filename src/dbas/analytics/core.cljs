(ns dbas.analytics.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as r :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<! timeout]]
            [dbas.analytics.highchart :as highchart]
            [dbas.analytics.utils :refer [time-ago]]))

(def dbas-base "https://dbas.cs.uni-duesseldorf.de")

(defn query-dbas [query]
  (http/get (str dbas-base "/api/v2/query?q=query{" query "}")))

;; -------------------------
;; Views

(defn argument-of-issues-chart
  "A component witch my fetch data periodically."
  [{:keys [refresh-timeout data timer] :or {timer true}}]
  (let [id (random-uuid)
        state (r/atom {:refreshed 0
                       :data (or data [])})
        refresh (fn [response]
                  (reset! state {:refreshed 0
                                 :data (mapv #(hash-map :name (:title  %)
                                                        :y (count (:statements %)))
                                             (get-in response [:body :issues]))}))]

    ; refresh first time
    (go (refresh (<! (query-dbas "issues{title,statements{uid}}"))))

    ; refresh further
    (when refresh-timeout
      (go-loop []
        (<! (timeout refresh-timeout))
        (refresh (<! (query-dbas "issues{title,statements{uid}}")))
        (recur)))

    ; tick timer
    (when timer
      (go-loop []
        (swap! state update :refreshed inc)
        (<! (timeout 1000))
        (recur)))

    ;render function. redraws every time 'state' is changed
    (fn []
      [:div.card
        [highchart/chart {:chart-meta {:id id}
                          :chart-data {:chart       {:type "pie"}
                                       :title       {:text "Statements of Issues"}
                                       :subtitle    {:text (str "Source: " dbas-base)}
                                       :plotOptions {:pie {:allowPointSelect true
                                                           :cursor           "pointer"
                                                           :dataLabels       {:enabled false
                                                                              :format  "<b>{point.name}</b>: {point.y}"}
                                                           :showInLegend     true}}
                                       :credits     {:enabled false}
                                       :series      [{:id 0
                                                      :name         "Statements"
                                                      :colorByPoint true
                                                      :data         (:data @state)}]}}]
        (when timer [:p.card-text [:small.text-muted "Last updated " (time-ago (:refreshed @state))]])])))


(defn home-page []
  [:div
   [:div.row.my-2
    [:div.col-md-6 [argument-of-issues-chart]]
    [:div.col-md-6 [argument-of-issues-chart {:refresh-timeout 5000}]]]])

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

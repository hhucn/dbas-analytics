(ns dbas.analytics.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as r :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<! timeout]]
            [dbas.analytics.highchart :as highchart]))

(def dbas-base "https://dbas.cs.uni-duesseldorf.de")

(defn query-dbas [query]
  (http/get (str dbas-base "/api/v2/query?q=query{" query "}")))

;; -------------------------
;; Views

(defn argument-of-issues-chart [{:keys [refresh-timeout]}]
  (let [id (random-uuid)
        data (r/atom [])]
    (go-loop [] (let [response (query-dbas "issues{title,statements{uid}}")
                      d (mapv #(hash-map :name (:title  %)
                                         :y (count (:statements %)))
                              (get-in (<! response) [:body :issues]))]
                  (reset! data d)
                  (when refresh-timeout
                    (<! (timeout refresh-timeout))
                    (recur))))
    (fn []
      [:div.card [highchart/chart {:chart-meta {:id id}
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
                                                               :data         @data}]}}]])))


(defn home-page []
  [:div.container
   [:div.row [:h1.col-md-12 "Welcome to dbas-analytics"]]
   [:div.row [:a {:href "/about"} "go to about page"]]
   [:div.row
    [:div.col-md-4 [argument-of-issues-chart]]
    [:div.col-md-4 [argument-of-issues-chart]]
    [:div.col-md-4 [argument-of-issues-chart {:refresh-timeout 5000}]]]
   [:div.row [:div.col-md-6 [argument-of-issues-chart]]
    [:div.col-md-6 [argument-of-issues-chart]]]])

(defn about-page []
  [:div [:h2 "About dbas-analytics"]
   [:div [:a {:href "/"} "go to the home page"]]])

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

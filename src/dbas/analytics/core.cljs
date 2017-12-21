(ns dbas.analytics.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [cljs-http.client :as http]
            [cljs.core.async :refer [chan <!]]))

(def dbas-base "https://dbas.cs.uni-duesseldorf.de")

(defn chart [chart-config]
  (r/create-class {:reagent-render      (fn [_] [:div {:style {:min-width "310px" :max-width "800px"
                                                               :height    "400px" :margin "0 auto"}}])
                   :component-did-mount #(js/Highcharts.Chart. (r/dom-node %) (clj->js chart-config))}))

(defn query-dbas [query]
  (http/get (str dbas-base "/api/v2/query?q=query{" query "}")))

(defn argument-of-issues-chart []
  (let [state (r/atom [:div]) ; holds an empty div until the fetching of the D-BAS data is done
        refresh (fn [] (go (let [response (query-dbas "issues{title,statements{uid}}")
                                 data (mapv #(hash-map :name (:title %)
                                                       :y (count (:statements %)))
                                            (get-in (<! response) [:body :issues]))]
                                (reset! state ; set the state with the following chart. Redraws automatically
                                        [chart {:chart       {:type "pie"}
                                                :title       {:text "Arguments of Issues"}
                                                :subtitle    {:text "Source: https://dbas.cs.uni-duesseldorf.de"}
                                                :plotOptions {:pie {:allowPointSelect true
                                                                    :cursor           "pointer"
                                                                    :dataLabels       {:enabled false
                                                                                       :format  "<b>{point.name}</b>: {point.y}"}
                                                                    :showInLegend     true}}
                                                :credits     {:enabled false}
                                                :series      [{:name         "Arguments"
                                                               :colorByPoint true
                                                               :data data}]}]))))]
    (refresh)
    (fn [] ; render function. If 'state' changes, the component is reloaded
      [:div @state])))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to dbas-analytics"]
   [:div [:a {:href "/about"} "go to about page"]
    [argument-of-issues-chart]]])

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

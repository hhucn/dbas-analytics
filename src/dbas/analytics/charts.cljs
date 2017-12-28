(ns dbas.analytics.charts
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [dbas.analytics.utils :refer [time-ago seconds minutes hours days]]
            [reagent.core :as r]
            [cljs-http.client :as http]
            [dbas.analytics.highchart :as highchart]
            [cljs.core.async :refer [<! timeout]]))

(def dbas-base "http://0.0.0.0:4284")
;(def dbas-base "https://dbas.cs.uni-duesseldorf.de")

(defn query-dbas [query]
  (http/get (str dbas-base "/api/v2/query?q=query{" query "}")))

(defn statements-of-issues-chart
  "A component witch may fetch data periodically."
  [{:keys [refresh-timeout data timer] :or {timer true}}]
  (let [query "issues(isDisabled:false){title,statements{uid}}"
        state (r/atom {:refreshed nil
                       :data      (or data [])})
        refresh (fn [response]
                  (reset! state {:refreshed 0
                                 :data      (mapv #(hash-map :name (:title %)
                                                             :y (count (:statements %)))
                                                  (get-in response [:body :issues]))}))]

    ; refresh first time
    (go (refresh (<! (query-dbas query))))

    ; refresh further
    (when refresh-timeout
      (go-loop []
               (<! (timeout refresh-timeout))
               (refresh (<! (query-dbas "issues(isDisabled:false){title,statements{uid}}")))
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
       [highchart/chart {:chart-data {:chart       {:type "pie"}
                                      :title       {:text "Statements of Issues"}
                                      :subtitle    {:text (str "Source: " dbas-base)}
                                      :plotOptions {:pie {:allowPointSelect true
                                                          :cursor           "pointer"
                                                          :dataLabels       {:enabled false
                                                                             :format  "<b>{point.name}</b>: {point.y}"}
                                                          :showInLegend     true}}
                                      :credits     {:enabled false}
                                      :series      [{:id           0
                                                     :name         "Statements"
                                                     :colorByPoint true
                                                     :data         (:data @state)}]}}]
       (when timer [:p.card-text [:small.text-muted "Last updated " (time-ago (:refreshed @state))]])])))


(defn arguments-of-issues [{:keys [refresh-timeout data timer] :or {data [] timer true}}]
  (let [query "issues(isDisabled:false){title,arguments(isDisabled:false){uid,isSupportive}}"
        state (r/atom {:refreshed nil
                       :issues    data})
        refresh (fn [response]
                  (let [issues (get-in response [:body :issues])
                        issue-list (mapv #(hash-map :name (:title %) :y (count (:arguments %))) issues)
                        get-pro-cons (fn [arguments] (frequencies (map :isSupportive arguments)))
                        supports (vec (mapcat #(vector {:name  "pro"
                                                        :y     (get % true)
                                                        :color "#00FF00"}
                                                       {:name  "con"
                                                        :y     (get % false)
                                                        :color "#FF0000"})
                                              (map get-pro-cons (map :arguments issues))))]
                    (reset! state {:refreshed 0
                                   :issues    issue-list
                                   :supports  supports})))]

    ; refresh first time
    (go (refresh (<! (query-dbas query))))

    ; refresh further
    (when refresh-timeout
      (go-loop []
               (<! (timeout refresh-timeout))
               (refresh (<! (query-dbas query)))
               (recur)))

    ; tick timer
    (when timer
      (go-loop []
               (swap! state update :refreshed inc)
               (<! (timeout 1000))
               (recur)))

    ;render function. redraws every time 'state' is changed
    (fn []
      (let [s @state]
        [:div.card
         [highchart/chart {:chart-data {:chart       {:type "pie"}
                                        :title       {:text "Supports for Arguments of Issues"}
                                        :subtitle    {:text (str "Source: " dbas-base)}
                                        :plotOptions {:pie {:allowPointSelect true
                                                            :cursor           "pointer"
                                                            :dataLabels       {:enabled false
                                                                               :format  "<b>{point.name}</b>: {point.y}"}
                                                            :showInLegend     true}}
                                        :credits     {:enabled false}
                                        :series      [{:id           "arguments"
                                                       :name         "Arguments"
                                                       :size         "100%"
                                                       :colorByPoint true
                                                       :data         (:issues s)
                                                       :showInLegend false}
                                                      {:id           "supports"
                                                       :name         "Supports"
                                                       :size         "100%"
                                                       :innerSize    "70%"
                                                       :data         (:supports s)
                                                       :showInLegend false}]}}]

         (when timer [:p.card-text [:small.text-muted "Last updated " (time-ago (:refreshed @state))]])]))))
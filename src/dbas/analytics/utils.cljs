(ns dbas.analytics.utils)

(defn time-ago [seconds]
  (let [units [{:name "second" :limit 60 :in-second 1}
               {:name "minute" :limit 3600 :in-second 60}
               {:name "hour" :limit 86400 :in-second 3600}
               {:name "day" :limit 604800 :in-second 86400}
               {:name "week" :limit 2629743 :in-second 604800}
               {:name "month" :limit 31556926 :in-second 2629743}
               {:name "year" :limit nil :in-second 31556926}]]
    (if (<= seconds 5)
      "just now"
      (let [unit (first (drop-while #(or (>= seconds (:limit %))
                                         (not (:limit %)))
                                    units))]
        (-> (/ seconds (:in-second unit))
            Math/floor
            int
            (#(str % " " (:name unit) (when (> % 1) "s") " ago")))))))
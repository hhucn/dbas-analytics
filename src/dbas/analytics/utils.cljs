(ns dbas.analytics.utils)

(defn seconds [v]
  (* v 1000))

(defn minutes [v]
  (* v 1000 60))

(defn hours [v]
  (* v 1000 60 60))

(defn days [v]
  (* v 1000 60 60 24))

(defn time-ago [secs]
  (if-not secs
    "never"
    (let [units [{:name "second" :limit 60 :in-second 1}
                 {:name "minute" :limit 3600 :in-second 60}
                 {:name "hour" :limit 86400 :in-second 3600}
                 {:name "day" :limit 604800 :in-second 86400}
                 {:name "week" :limit 2629743 :in-second 604800}
                 {:name "month" :limit 31556926 :in-second 2629743}
                 {:name "year" :limit nil :in-second 31556926}]]
      (if (< secs 6)
        "just now"
        (let [unit (first (drop-while #(or (>= secs (:limit %))
                                           (not (:limit %)))
                                      units))]
          (-> (/ secs (:in-second unit))
              Math/floor
              int
              (#(str % " " (:name unit) (when (> % 1) "s ago")))))))))


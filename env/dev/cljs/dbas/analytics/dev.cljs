(ns ^:figwheel-no-load dbas.analytics.dev
  (:require
    [dbas.analytics.core :as core]
    [devtools.core :as devtools]))


(enable-console-print!)

(devtools/install!)

(core/init!)

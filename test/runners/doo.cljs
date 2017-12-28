(ns runners.doo
  (:require [doo.runner :refer-macros [doo-tests doo-all-tests]]
            [runners.tests]))


;(doo-tests 'dbas.analytics.core-test)
(doo-all-tests #"(dbas.analytics)\..*-test")
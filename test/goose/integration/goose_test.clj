(ns goose.integration.goose-test
  (:require
   [goose.integration.utils :as u :refer [executable
                                          delivered-execution
                                          def-integration-test]]
   [goose.client :as c]
   [goose.worker :as w]
   [clojure.test :refer [is]])
  (:import
   [java.time Instant]
   [java.util UUID]))

(def-integration-test async-execution-test
  #{:enqueue}
  (let [job (c/perform-async (u/get-opts broker :client)
                             `executable
                             test-name
                             ::async-execution-test)
        worker (w/start (u/get-opts broker :worker))]
    (is (uuid? (UUID/fromString (:id job))))
    (is (= ::async-execution-test
           (delivered-execution test-name)))
    (w/stop worker)))

(def-integration-test absolute-scheduling-test
  #{:schedule}
  (let [job (c/perform-at (u/get-opts broker :client)
                          (Instant/now)
                          `executable
                          test-name
                          ::absolute-scheduling-test)
        scheduler (w/start (u/get-opts broker :worker))]
    (is (uuid? (UUID/fromString (:id job))))
    (is (= ::absolute-scheduling-test
           (delivered-execution test-name)))
    (w/stop scheduler)))

(def-integration-test relative-scheduling-test
  #{:schedule}
  (let [job (c/perform-in-sec (u/get-opts broker :client)
                              1
                              `executable
                              test-name
                              ::relative-scheduling-test)
        scheduler (w/start (u/get-opts broker :worker))]
    (is (uuid? (UUID/fromString (:id job))))
    (is (= ::relative-scheduling-test
           (delivered-execution test-name)))
    (w/stop scheduler)))

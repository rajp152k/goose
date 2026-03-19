(ns goose.integration.async-execution-test
  (:require
   [goose.temp]
   [goose.client :as c]
   [goose.worker :as w]
   [goose.test-utils :as tu]
   [clojure.test :refer [deftest is testing use-fixtures run-test]]))

(use-fixtures :each tu/redis-fixture)

(defn temp-intern-function [function-symbol function]
  (symbol (intern 'goose.temp function-symbol function)))

(deftest perform-async-test
  (testing "Goose executes a function asynchronously"
    (let [res (promise)
          deliver-fn (temp-intern-function 'deliver-fn
                                           #(deliver res %))]
      (c/perform-async tu/redis-client-opts
                       deliver-fn :package)
      (let [worker (w/start tu/redis-worker-opts)]
        (is (= (deref res 100 :timed-out) :package))
        (w/stop worker)))))

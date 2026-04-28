(ns goose.integration.async-execution-test
  (:require
   [goose.integration.test-utils :as tu :refer [broker-testable? with-fixtures def-integration-test]]
   [goose.client :as c]
   [goose.worker :as w]
   [clojure.test :refer [is]]))

(def requirements #{:enqueue})

(def perform-async-fn-executed (atom nil))

(defn perform-async-fn [arg]
  (deliver @perform-async-fn-executed arg))

(comment
  (deftest  async-execution-test
    (doseq [broker (keys tu/broker-utils)]
      (alter-meta! #'async-execution-test assoc :name (str (symbol broker) "-async-execution-test"))
      (if (tu/broker-testable? broker requirements)
        (tu/with-fixtures broker
          (fn [ex] (report {:type :default
                            :message (ex-message ex)}))
          (testing (str "Async Execution" broker)
            (reset! perform-async-fn-executed (promise))
            (let [_ (c/perform-async (tu/get-opts broker :client)
                                     `perform-async-fn
                                     ::async-execution-test)
                  worker (w/start (tu/get-opts broker :worker))]
              (is (= ::async-execution-test (deref  @perform-async-fn-executed 100 :async-execution-test-timed-out)))
              (w/stop worker))))
        (report {:type :default
                 :message (str "Async execution" broker " is not testable")})))))


(def-integration-test async-execution-test requirements
  (reset! perform-async-fn-executed (promise))
  (let [_ (c/perform-async (tu/get-opts broker :client)
                           `perform-async-fn
                           ::async-execution-test)
        worker (w/start (tu/get-opts broker :worker))]
    (is (= ::async-execution-test (deref  @perform-async-fn-executed 100 :async-execution-test-timed-out)))
    (w/stop worker)))

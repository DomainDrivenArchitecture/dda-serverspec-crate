; Licensed to the Apache Software Foundation (ASF) under one
; or more contributor license agreements. See the NOTICE file
; distributed with this work for additional information
; regarding copyright ownership. The ASF licenses this file
; to you under the Apache License, Version 2.0 (the
; "License"); you may not use this file except in compliance
; with the License. You may obtain a copy of the License at
;
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns dda.pallet.dda-serverspec-crate.infra.test.http
  (:require
    [schema.core :as s]
    [dda.pallet.dda-serverspec-crate.infra.fact.http :as http-fact]
    [dda.pallet.dda-serverspec-crate.infra.core.test :as server-test]))

(def HttpTestConfig {s/Keyword {:expiration-days s/Num}})

(s/defn fact-check :- server-test/TestResult
  "Compare facts & expectation in order to return test-results."
  [result :- server-test/TestResult
   spec :- HttpTestConfig
   fact-map]
  (if (<= (count spec) 0)
    result
    (let [elem (first spec)
          expected-expiration-days (:expiration-days (val elem))
          fact-elem  (get-in fact-map [(key elem)])
          fact-expiration-days (:expiration-days fact-elem)
          passed? (<= expected-expiration-days fact-expiration-days)]
        (recur
          {:test-passed (and (:test-passed result) passed?)
           :test-message (str (:test-message result) "test http: " (name (key elem))
                              ", expected:: min expiration-days: " expected-expiration-days
                              " - found facts:: expiration-days: "
                              (if (= fact-expiration-days -1)
                                "---ERROR RETRIEVING EXPIRATION---"
                                fact-expiration-days)
                              " - passed?: " passed? "\n")
           :no-passed (if passed? (inc (:no-passed result)) (:no-passed result))
           :no-failed (if (not passed?) (inc (:no-failed result)) (:no-failed result))}
          (rest spec)
          fact-map))))

(s/defn test-http-internal :- server-test/TestResultHuman
  "Exposing fact input to signature for tests."
  [test-config :- HttpTestConfig
   input :- {s/Keyword http-fact/HttpFactResult}]
  (let [fact-result (fact-check server-test/fact-check-seed test-config input)]
    (server-test/fact-result-to-test-result input fact-result)))

(s/defn test-http :- server-test/TestActionResult
  "The delayed action to be called in test phase.
  Getting upfront the facts from session."
  [test-config :- HttpTestConfig]
  (server-test/test-it
    http-fact/fact-id-http
    #(test-http-internal test-config %)))

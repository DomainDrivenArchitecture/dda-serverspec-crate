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
(ns dda.pallet.dda-serverspec-crate.infra.test.netcat
  (:require
   [schema.core :as s]
   [dda.pallet.dda-serverspec-crate.infra.fact.netcat :as netcat-fact]
   [dda.pallet.dda-serverspec-crate.infra.core.test :as server-test]))


(def NetcatTestConfig
  {s/Keyword {:reachable? s/Bool}})

(s/defn fact-check
  "Compare facts & expectation in order to return test-results."
  [result :- server-test/FactCheckResult
   spec :- NetcatTestConfig
   fact-map]
  (if (<= (count spec) 0)
    result
    (let [elem (first spec)
          expected-reachable? (:reachable? (val elem))
          fact-elem (get-in fact-map [(key elem)])
          fact-reachable? (:reachable? fact-elem)
          passed? (= fact-reachable? expected-reachable?)]
      (recur
       {:test-passed (and (:test-passed result) passed?)
        :test-message (str (:test-message result) "test host: " (name (key elem))
                           ", expected reachable?: " expected-reachable? ", was reachable?: "
                           fact-reachable? ", passed?: " passed? "\\n ")
        :no-passed (if passed? (inc (:no-passed result)) (:no-passed result))
        :no-failed (if (not passed?) (inc (:no-failed result)) (:no-failed result))}
       (rest spec)
       fact-map))))

(s/defn test-netcat-internal :- server-test/TestResult
  [test-config :- NetcatTestConfig
   input :- {s/Keyword netcat-fact/NetcatFactResults}]
  (let [fact-result (fact-check server-test/fact-check-seed test-config input)]
    (server-test/fact-result-to-test-result input fact-result)))

(s/defn test-netcat :- server-test/TestActionResult
  [test-config :- NetcatTestConfig]
  (server-test/test-it
    netcat-fact/fact-id-netcat
    #(test-netcat-internal test-config %)))

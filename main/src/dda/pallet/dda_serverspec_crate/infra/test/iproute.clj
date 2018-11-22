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

(ns dda.pallet.dda-serverspec-crate.infra.test.iproute
  (:require
    [schema.core :as s]
    [dda.config.commons.schema :as schema]
    [dda.pallet.dda-serverspec-crate.infra.fact.iproute :as iproute-fact]
    [dda.pallet.dda-serverspec-crate.infra.core.test :as server-test]))

(def IprouteTestConfig {s/Keyword {:via (s/pred schema/ipv4?)
                                   :source s/Str}})

(s/defn fact-check :- server-test/TestResult
  "Compare facts & expectation in order to return test-results."
  [result :- server-test/TestResult
   spec :- IprouteTestConfig
   fact-map]
  (if (<= (count spec) 0)
    result
    (let [elem (first spec)
          {expected-via :via expected-source :source} (val elem)
          fact-elem (get-in fact-map [(key elem)])
          fact-via (:via fact-elem)
          passed? (= fact-via expected-via)]
      (recur
       {:test-passed (and (:test-passed result) passed?)
        :test-message (str (:test-message result) "test host: " (name (key elem))
                           ", expected: " (pr-str expected-source)
                           ", via: " (pr-str expected-via)
                           " - found facts:: via: " (pr-str fact-via)
                           " - passed?: " passed? "\n")
        :no-passed (if passed? (inc (:no-passed result)) (:no-passed result))
        :no-failed (if (not passed?) (inc (:no-failed result)) (:no-failed result))}
       (rest spec)
       fact-map))))

(s/defn test-iproute-internal :- server-test/TestResultHuman
  "Exposing fact input to signature for tests."
  [test-config :- IprouteTestConfig
   input :- {s/Keyword iproute-fact/IprouteFactResults}]
  (let [fact-result (fact-check server-test/fact-check-seed test-config input)]
    (server-test/fact-result-to-test-result input fact-result)))

(s/defn test-iproute :- server-test/TestActionResult
  "The delayed action to be called in test phase.
Getting upfront filled facts from session."
  [test-config :- IprouteTestConfig]
  (server-test/test-it
    iproute-fact/fact-id-iproute
    #(test-iproute-internal test-config %)))

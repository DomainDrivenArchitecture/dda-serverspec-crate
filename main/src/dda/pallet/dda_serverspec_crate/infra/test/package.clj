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
(ns dda.pallet.dda-serverspec-crate.infra.test.package
  (:require
    [schema.core :as s]
    [dda.pallet.dda-serverspec-crate.infra.core.test :as server-test]
    [dda.pallet.dda-serverspec-crate.infra.fact.package :as package-fact]))

(def PackageTestConfig {s/Keyword {:installed? s/Bool}})

(s/defn fact-check :- server-test/TestResult
  [result :- server-test/TestResult
   spec :- PackageTestConfig
   considered-map]
  (if (<= (count spec) 0)
    result
    (let [elem (first spec)
          expected-installed? (:installed? (val elem))
          is-installed?  (contains? considered-map (key elem))
          passed? (= expected-installed? is-installed?)]
        (recur
          {:test-passed (and (:test-passed result) passed?)
           :test-message (str (:test-message result) "test package: " (name (key elem))
                              ", expected:: installed?: " expected-installed?
                              " - found facts:: installed?: " is-installed?
                              " - passed?: " passed? "\n")
           :no-passed (if passed? (inc (:no-passed result)) (:no-passed result))
           :no-failed (if (not passed?) (inc (:no-failed result)) (:no-failed result))}
          (rest spec)
          considered-map))))

(s/defn filter-input-to-consider :- package-fact/PackageResults
  [test-config :- PackageTestConfig
   input :- package-fact/PackageResults]
  (let [installed-to-consider (filter #(= "ii" (:state %)) input)]
    (filter #(contains? test-config (keyword (:package %))) installed-to-consider)))

(s/defn result-to-map
  [input :- package-fact/PackageResults]
  (apply merge (map (fn [e] {(keyword (:package e)) e}) input)))

(s/defn test-package-internal :- server-test/TestResultHuman
  [test-config :- PackageTestConfig
   input :- package-fact/PackageResults]
  (let [input-to-consider (filter-input-to-consider test-config input)
        considered-map (result-to-map input-to-consider)
        fact-result (fact-check server-test/fact-check-seed test-config considered-map)]
    (server-test/fact-result-to-test-result input fact-result)))

(s/defn test-package :- server-test/TestActionResult
  [test-config :- PackageTestConfig]
  (server-test/test-it
    package-fact/fact-id-package
    #(test-package-internal test-config %)))

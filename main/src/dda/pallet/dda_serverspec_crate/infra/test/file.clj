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

(ns dda.pallet.dda-serverspec-crate.infra.test.file
  (:require
    [schema.core :as s]
    [dda.pallet.dda-serverspec-crate.infra.fact.file :as file-fact]
    [dda.pallet.dda-serverspec-crate.infra.core.test :as server-test]))

(def FileTestConfig {s/Keyword {:exist? s/Bool
                                (s/optional-key :mod) s/Str
                                (s/optional-key :user) s/Str
                                (s/optional-key :group) s/Str
                                (s/optional-key :type) s/Str
                                (s/optional-key :link-to) s/Str}})

(s/defn fact-check :- server-test/TestResult
  "Compare facts & expectation in order to return test-results."
  [result :- server-test/TestResult
   spec :- FileTestConfig
   considered-map]
  (if (<= (count spec) 0)
    result
    (let [elem (first spec)
          {:keys [exist? mod user group type link-to]} (val elem)
          present-elem  (get-in considered-map [(key elem)])
          {:keys [fact-exist? fact-mod fact-user fact-group fact-type fact-link-to]} present-elem
          test-mod   (contains? (val elem) :mod)
          test-user  (contains? (val elem) :user)
          test-group (contains? (val elem) :group)
          test-type (contains? (val elem) :type)
          test-link-to (contains? (val elem) :link-to)
          passed? (and
                    (= exist? fact-exist?)
                    (if test-mod (= mod fact-mod) true)
                    (if test-user (= user fact-user) true)
                    (if test-group (= group fact-group) true)
                    (if test-type (= type fact-type) true)
                    (if test-link-to (= link-to fact-link-to) true))
          expected-settings (str ", expected:: exists?: " exist?
                                 (if test-mod (str ", mod: " mod) "")
                                 (if test-user (str ", user: " user) "")
                                 (if test-group (str ", group: " group) "")
                                 (if test-type (str ", type: " type) "")
                                 (if test-link-to (str ", link-to: " link-to) ""))
          actual-settings (if fact-exist?
                            (str " - found facts:: exist?: true, mod: " fact-mod ", user: " fact-user ", group: "
                                 fact-group ", type: " fact-type ", link-to: " fact-link-to)
                            " - found facts:: exist?: false")]
        (recur
          {:test-passed (and (:test-passed result) passed?)
           :test-message (str (:test-message result) "test file: " (name (key elem))
                              expected-settings
                              actual-settings
                              " - passed?: " passed? "\n")
           :no-passed (if passed? (inc (:no-passed result)) (:no-passed result))
           :no-failed (if (not passed?) (inc (:no-failed result)) (:no-failed result))}
          (rest spec)
          considered-map))))

(s/defn test-file-internal :- server-test/TestResultHuman
  "Exposing fact input to signature for tests."
  [test-config :- FileTestConfig
   input :- {s/Keyword file-fact/FileFactResult}]
  (let [fact-result (fact-check server-test/fact-check-seed test-config input)]
    (server-test/fact-result-to-test-result input fact-result)))

(s/defn test-file :- server-test/TestActionResult
  "The delayed action to be called in test phase.
Getting upfront filled facts from session."
  [test-config :- FileTestConfig]
  (server-test/test-it
    file-fact/fact-id-file
    #(test-file-internal test-config %)))

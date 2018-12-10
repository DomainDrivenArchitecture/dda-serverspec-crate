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

(ns dda.pallet.dda-serverspec-crate.infra.test.iproute-test
  (:require
    [clojure.test :refer :all]
    [clojure.test.check :as tc]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]
    [dda.pallet.dda-serverspec-crate.infra.test.iproute :as sut]))

; -----------------------  test data  --------------------------

(def prop-map-via-equivalence
  (prop/for-all
   [m (gen/hash-map :via gen/string)]
   (= 0 (:no-failed (sut/test-iproute-internal m m)))))

(def prop-pass-empty
  (prop/for-all
   [m (gen/hash-map :via gen/string)]
   (= 0 (:no-failed (sut/test-iproute-internal {} m)))))

; -----------------------  tests  --------------------------

(deftest test-iproute-internal
  (testing
    "test test-iproute-internal"
    (is (:result (tc/quick-check 10 prop-pass-empty)))
    (is (:result (tc/quick-check 10 prop-map-via-equivalence)))))

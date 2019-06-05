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
(ns dda.pallet.dda-serverspec-crate.infra.fact.http-test
  (:require
   [clojure.test :refer :all]
   [data-test :refer :all]
   [clojure.tools.logging :as logging]
   [dda.pallet.dda-serverspec-crate.infra.fact.http :as sut]))

; ------------------------  test data  ------------------------
(def reference-date
  (java.time.LocalDate/parse "10.02.2018"
    (java.time.format.DateTimeFormatter/ofPattern "dd.MM.yyyy")))

(def date-2018-04-15
  (java.time.LocalDate/parse "15.04.2018"
    (java.time.format.DateTimeFormatter/ofPattern "dd.MM.yyyy")))

(def date-2020-02-06-12-00
  (java.time.LocalDate/parse "06.02.2020 12:00:00"
    (java.time.format.DateTimeFormatter/ofPattern "dd.MM.yyyy hh:mm:ss")))

(def date-offset
  (try
    (.between (java.time.temporal.ChronoUnit/DAYS)
              reference-date
              (java.time.LocalDate/now))
    (catch java.time.DateTimeException ex
      (logging/warn "Exception encountered : " ex))))

(def fact-756 {:_some_url {:expiration-days (- 756 date-offset)}})

(def fact-bahn-and-google
  {:https___bahn.de {:expiration-days (- 756 date-offset)}
   :https___google.com {:expiration-days (- 66 date-offset)}})

(def fact-64 {:https___domaindrivenarchitecture.org {:expiration-days (- 64 date-offset)}})

(def fact-194 {:https___domaindrivenarchitecture.org {:expiration-days (- 194 date-offset)}})

; ------------------------  tests  ------------------------------
(deftest test-parse-date
  (testing
    "test parsing http output"
    (is (= date-2018-04-15
           (sut/parse-date-16-04 "Sun, 15 Apr 2018 12:01:04 GMT")))
    (is (= date-2018-04-15
           (sut/parse-date-18-04 "Apr 15 12:01:04 2018 GMT")))
    (is (= date-2020-02-06-12-00
           (sut/parse-date-18-04 "Feb  6 12:00:00 2020 GMT")))))

(defdatatest should-parse-756 [input _]
  (is (= fact-756
         (sut/parse-http-response input))))

(defdatatest should-parse-bahn-and-google [input _]
  (is (= fact-bahn-and-google
         (sut/parse-http-script-responses (str (:google input)
                                        sut/output-separator
                                        (:bahn input)
                                        sut/output-separator)))))

(defdatatest should-parse-invalid [input expected]
  (is (= expected
         (sut/parse-http-script-responses input))))

(defdatatest should-parse-64 [input _]
  (is (= fact-64
         (sut/parse-http-script-responses input))))

(defdatatest should-parse-194 [input _]
  (is (= fact-194
         (sut/parse-http-script-responses input))))

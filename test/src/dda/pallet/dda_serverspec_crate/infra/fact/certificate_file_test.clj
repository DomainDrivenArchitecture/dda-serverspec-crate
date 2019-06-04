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

(ns dda.pallet.dda-serverspec-crate.infra.fact.certificate-file-test
  (:require
   [clojure.test :refer :all]
   [data-test :refer :all]
   [dda.pallet.dda-serverspec-crate.infra.fact.certificate-file :as sut]))

(deftest should-parse-oldscool
  (is (= {:_somedir_cert.pem {:expiration-days 123}
          :_someotherdir_cert2.pem {:expiration-days 789}}
         (sut/parse-certificate-script-responses 
          (str
           "_somedir_cert.pem\n123\n"
           sut/output-separator
           "_someotherdir_cert2.pem\n789\n"
           sut/output-separator)))))

(defdatatest should-parse [plain-input fact-expected]
  (is (= fact-expected
          (sut/parse-certificate-script-responses plain-input))))

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

(ns dda.pallet.dda-serverspec-crate.domain-test
  (:require
    [clojure.test :refer :all]
    [dda.pallet.dda-serverspec-crate.domain :as sut]))

; -----------------------  test data  ------------------------
(def domain-config-1
  {:netstat '({:process-name "sshd" :port "22"})
   :package '({:name "firefox" :installed? false})
   :file [{:path "/root"}
          {:path "/etc" :exist? true}
          {:path "/absent" :exist? false}]
   :netcat '({:host "www.google.com" :port 80}
             {:host "www.google.c" :port 80 :reachable? false})})

(def domain-config-2
  {:file '({:path "/etc"}
           {:path "/nonexist/sth" :exist? false})})

(def domain-config-certificate-file-test
  {:certificate-file '({:file "/etc/ssl/crt/primary.crt"
                        :expiration-days 33}
                       {:file "/etc/ssl/crt/nonvalid.crt"
                        :expiration-days 22})})

(def domain-config-http-test
  {:http '({:url "https://google.com"
            :expiration-days 33}
           {:url "http://bahn.de"
            :expiration-days 22})})

; ------------------------  tests  ---------------------------
(deftest test-infra-configuration
  (testing
    "test creation of infra configuration"
    (is (=  {:dda-servertest
              {:netcat-fact {:www.google.com_80_8 {:host "www.google.com" :port 80 :timeout 8}
                             :www.google.c_80_8 {:host "www.google.c" :port 80 :timeout 8}}
               :netstat-fact nil
               :package-fact nil
               :file-fact {:_root {:path "/root"}
                           :_etc {:path "/etc"}
                           :_absent {:path "/absent"}}
               :netstat-test {:sshd_tcp_0.0.0.0:22 {:port "22" :ip "0.0.0.0" :exp-proto "tcp" :running? true}}
               :package-test {:firefox {:installed? false}}
               :file-test {:_root {:exist? true}
                           :_etc {:exist? true},
                           :_absent {:exist? false}}
               :netcat-test {:www.google.com_80_8 {:reachable? true}
                             :www.google.c_80_8 {:reachable? false}}}}
          (sut/infra-configuration domain-config-1)))
    (is (=  {:dda-servertest
              {:file-fact {:_etc {:path "/etc"}
                           :_nonexist_sth {:path "/nonexist/sth"}}
               :file-test {:_etc {:exist? true},
                           :_nonexist_sth {:exist? false}}}}
          (sut/infra-configuration domain-config-2)))))

(deftest test-certificate-file-configuration
  (testing
    "test creation of infra configuration"
    (is (=  {:dda-servertest
              {:certificate-fact {:_etc_ssl_crt_primary.crt {:file "/etc/ssl/crt/primary.crt"}
                                  :_etc_ssl_crt_nonvalid.crt {:file "/etc/ssl/crt/nonvalid.crt"}}
               :certificate-test {:_etc_ssl_crt_primary.crt {:expiration-days 33},
                                  :_etc_ssl_crt_nonvalid.crt {:expiration-days 22}}}}
          (sut/infra-configuration domain-config-certificate-file-test)))))

(deftest test-http-configuration
  (testing
    "test creation of infra configuration"
    (is (=  {:dda-servertest
              {:http-fact
               {:https___google.com {:url "https://google.com"},
                :http___bahn.de {:url "http://bahn.de"}},
               :http-test
               {:https___google.com {:expiration-days 33},
                :http___bahn.de {:expiration-days 22}}}}
            (sut/infra-configuration domain-config-http-test)))))

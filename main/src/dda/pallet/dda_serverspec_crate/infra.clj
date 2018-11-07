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

(ns dda.pallet.dda-serverspec-crate.infra
  (:require
   [clojure.tools.logging :as logging]
   [schema.core :as s]
   [pallet.api :as api]
   [pallet.actions :as actions]
   [dda.pallet.core.infra :as core-infra]
   [dda.pallet.dda-serverspec-crate.infra.core.fact :as core-fact]
   [dda.pallet.dda-serverspec-crate.infra.fact.package :as package-fact]
   [dda.pallet.dda-serverspec-crate.infra.fact.netstat :as netstat-fact]
   [dda.pallet.dda-serverspec-crate.infra.fact.file :as file-fact]
   [dda.pallet.dda-serverspec-crate.infra.fact.netcat :as netcat-fact]
   [dda.pallet.dda-serverspec-crate.infra.fact.certificate-file :as certificate-file-fact]
   [dda.pallet.dda-serverspec-crate.infra.fact.http :as http-fact]
   [dda.pallet.dda-serverspec-crate.infra.fact.iproute :as iproute-fact]
   [dda.pallet.dda-serverspec-crate.infra.fact.command :as command-fact]
   [dda.pallet.dda-serverspec-crate.infra.core.test :as core-test]
   [dda.pallet.dda-serverspec-crate.infra.test.package :as package-test]
   [dda.pallet.dda-serverspec-crate.infra.test.netstat :as netstat-test]
   [dda.pallet.dda-serverspec-crate.infra.test.file :as file-test]
   [dda.pallet.dda-serverspec-crate.infra.test.netcat :as netcat-test]
   [dda.pallet.dda-serverspec-crate.infra.test.certificate-file :as certificate-file-test]
   [dda.pallet.dda-serverspec-crate.infra.test.http :as http-test]
   [dda.pallet.dda-serverspec-crate.infra.test.iproute :as iproute-test]
   [dda.pallet.dda-serverspec-crate.infra.test.command :as command-test]))

; -----------------------  fields and schemas  ----------------------
(def facility :dda-servertest)

(def fact-facility core-fact/fact-facility)

(def fact-id-file file-fact/fact-id-file)

(def ServerTestConfig
  {(s/optional-key :package-fact) s/Any       ; parsed result of "netstat -tulpen". Any is ignored.
   (s/optional-key :netstat-fact) s/Any       ; parsed result of "dpkg -l". Any is ignored.
   (s/optional-key :file-fact)                ; parsed result of "find [path] -prune -printf \"%p'%s'%u'%g'%m'%y'%c'%t'%a\\n\"
   file-fact/FileFactConfig
   (s/optional-key :netcat-fact)              ; parsed result of "nc [host] -w [timeout] && echo $?"
   netcat-fact/NetcatFactConfig
   (s/optional-key :certificate-file-fact) certificate-file-fact/CertificateFileFactConfig
   (s/optional-key :http-fact) http-fact/HttpFactConfig
   (s/optional-key :iproute-fact) iproute-fact/IprouteFactConfig
   (s/optional-key :command-fact) command-fact/CommandFactConfig
   (s/optional-key :package-test) package-test/PackageTestConfig
   (s/optional-key :netstat-test) netstat-test/NetstatTestConfig
   (s/optional-key :file-test) file-test/FileTestConfig
   (s/optional-key :netcat-test) netcat-test/NetcatTestConfig
   (s/optional-key :certificate-file-test) certificate-file-test/CertificateFileTestConfig
   (s/optional-key :http-test) http-test/HttpTestConfig
   (s/optional-key :iproute-test) iproute-test/IprouteTestConfig
   (s/optional-key :command-test) command-test/CommandTestConfig      ; the expected exit code or output for specified command
   })

; -----------------------  functions and methods  ------------------------
(s/defn ^:always-validate
 path-to-keyword :- s/Keyword
 [path :- s/Str]
 (file-fact/path-to-keyword path))

(s/defn ^:always-validate
  ip-to-keyword :- s/Keyword
  [ip :- s/Str]
  (iproute-fact/ip-to-keyword ip))

(s/defn ^:always-validate
 config-to-string :- s/Str
 [host :- s/Str port :- s/Num timeout :- s/Num]
 (netcat-fact/config-to-string host port timeout))

(s/defn ^:always-validate
 certificate-file-to-keyword :- s/Keyword
 [file :- s/Str]
 (certificate-file-fact/certificate-file-to-keyword file))

(s/defn ^:always-validate
 url-to-keyword :- s/Keyword
 [url :- s/Str]
 (http-fact/url-to-keyword url))

(s/defn ^:always-validate
 command-to-keyword :- s/Keyword
 [command :- s/Str]
 (command-fact/command-to-keyword command))

(s/defmethod core-infra/dda-settings facility
  [core-infra config]
  "dda-serverspec: setting"
  (let [{:keys [file-fact netcat-fact certificate-file-fact
                http-fact iproute-fact command-fact]} config]
    (when (contains? config :package-fact)
      (package-fact/collect-package-fact))
    (when (contains? config :netstat-fact)
      (netstat-fact/collect-netstat-fact))
    (when (contains? config :file-fact)
      (file-fact/collect-file-fact file-fact))
    (when (contains? config :netcat-fact)
      (netcat-fact/collect-netcat-fact netcat-fact))
    (when (contains? config :certificate-file-fact)
      (certificate-file-fact/collect-certificate-file-fact certificate-file-fact))
    (when (contains? config :http-fact)
      (http-fact/collect-http-fact http-fact))
    (when (contains? config :iproute-fact)
      (iproute-fact/collect-iproute-fact iproute-fact))
    (when (contains? config :command-fact)
      (command-fact/collect-command-fact command-fact))))

(s/defmethod core-infra/dda-install facility
  [dda-crate config]
  "dda-serverspec: install"
  (when (contains? config :http-fact)
    (http-fact/install)))

(s/defmethod core-infra/dda-test facility
  [dda-crate config]
  (when (contains? config :package-test)
    (package-test/test-package (:package-test config)))
  (when (contains? config :netstat-test)
    (netstat-test/test-netstat (:netstat-test config)))
  (when (contains? config :file-test)
    (file-test/test-file (:file-test config)))
  (when (contains? config :netcat-test)
    (netcat-test/test-netcat (:netcat-test config)))
  (when (contains? config :certificate-file-test)
    (certificate-file-test/test-certificate-file (:certificate-file-test config)))
  (when (contains? config :http-test)
    (http-test/test-http (:http-test config)))
  (when (contains? config :iproute-test)
    (iproute-test/test-iproute (:iproute-test config)))
  (when (contains? config :command-test)
    (command-test/test-command (:command-test config))))

(def dda-serverspec-crate
  (core-infra/make-dda-crate-infra
   :facility facility))

(def with-serverspec
  (core-infra/create-infra-plan dda-serverspec-crate))

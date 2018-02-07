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

(ns dda.pallet.dda-serverspec-crate.infra.fact.certificate
  (:require
   [schema.core :as s]
   [clojure.string :as string]
   [dda.pallet.dda-serverspec-crate.infra.core.fact :refer :all]))

; -----------------------  fields & schemas  ------------------------
(def fact-id-certificate ::certificate)

(def CertificateFactConfig
  {s/Keyword {:file s/Str}})         ;with full path   TODO repl with file type??

(def CertificateFactResult {:expiration-days s/Num})

(def CertificateFactResults {s/Keyword CertificateFactResult})

(def output-separator "----- certificate output end -----\n")

; -----------------------  functions  -------------------------------
(s/defn certificate-file-to-keyword :- s/Keyword
  "creates a keyword from the certificate file-path"
  [file :- s/Str] (keyword (clojure.string/replace file #"/" "_")))

(s/defn build-certificate-script
  "builds the script to check the expiration days certificate"
  [certificate-config :- CertificateFactConfig]
  (let [config-val (val certificate-config)
        config-key (key certificate-config)
        {:keys [file]} config-val]
    (str
      "echo '" (name config-key) "';"
      "echo $(( ( $(date --date=\"$(openssl x509 -in "
      file
      " -noout -enddate | cut -d= -f 2)\" \"+%s\") - $(date \"+%s\") ) / 86400));"
      "echo -n '" output-separator "'")))

(s/defn parse-certificate-response :- CertificateFactResult
  "returns a CertificateFactResult from the result text of one certificate check"
  [single-script-result]
  (let [result-lines (string/split single-script-result #"\n")
        result-key (first result-lines)
        result-text (nth result-lines 1)
        ;convert to number or nil:
        result-number (re-find #"^\d+$" result-text)]
    (if result-number
      {(keyword result-key) {:expiration-days (Integer. result-number)}}
      {(keyword result-key) {:expiration-days -1}})))

(defn parse-certificate-script-responses
  "returns a CertificateFactResult from the result text of one certificate check"
  [raw-script-results]
  (apply merge
    (map parse-certificate-response (clojure.string/split raw-script-results (re-pattern output-separator)))))

(s/defn collect-certificate-fact
  "Collects the facts for the certificates using the script"
  [fact-config :- CertificateFactConfig]
  (collect-fact
    fact-id-certificate
    (str
      (clojure.string/join
       "; " (map #(build-certificate-script %) fact-config))
      "; exit 0")
    :transform-fn parse-certificate-script-responses))

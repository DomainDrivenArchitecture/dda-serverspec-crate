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

(ns dda.pallet.dda-serverspec-crate.infra.fact.http
  (:require
    [clojure.string :as string]
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.actions :as actions]
    [dda.pallet.dda-serverspec-crate.infra.core.fact :refer :all]))

; -----------------------  fields & schemas  ------------------------
(def fact-id-http ::http)

(def HttpFactConfig {s/Keyword {:url s/Str}})   ;full url e.g. https://google.com

(def HttpFactResult {:expiration-days s/Num})   ;value -1 in case of error

(def HttpFactResults {s/Keyword HttpFactResult})

(def output-separator "----- http output separator -----\n")

; -----------------------  functions  -------------------------------
(s/defn url-to-keyword :- s/Keyword
  "creates a keyword from a url"
  [url :- s/Str] (keyword (string/replace url #"[:/]" "_")))

(s/defn build-http-script
  "builds the script to retrieve the http response from a url"
  [http-config] ;- MapEntry of HttpFactConfig
  (let [config-val (val http-config)
        config-key (key http-config)
        {:keys [url]} config-val]
    (str
      "echo '" (name config-key) "';"
      "LC_ALL=C curl --head --insecure --trace-ascii - " url ";"
      "echo -n '" output-separator "'")))

(s/defn preprocess-date-string :- s/Str
  "`date` can emit an extra space before date instead of zero or nothing: 'Feb  1'"
  [date-string :- s/Str]
  (string/replace date-string #" +" " "))

(s/defn parse-date-16-04 :- java.time.LocalDate
  [date-string :- s/Str]
  (let [date-format "EEE, d MMM yyyy HH:mm:ss z"
        locale (java.util.Locale. "en" "US")];
    (java.time.LocalDate/parse (preprocess-date-string date-string)
      (java.time.format.DateTimeFormatter/ofPattern date-format locale))))

(s/defn parse-date-18-04 :- java.time.LocalDate
  [date-string :- s/Str]
  (let [date-format "MMM d HH:mm:ss yyyy z"
        locale (java.util.Locale. "en" "US")];
    (java.time.LocalDate/parse (preprocess-date-string date-string)
      (java.time.format.DateTimeFormatter/ofPattern date-format locale))))

(s/defn parse-http-response :- HttpFactResult
  "returns a HttpFactResult from the result text of one http check"
  [single-script-result :- s/Str]
  (let [result-lines (string/split single-script-result #"\n" 2)
        result-key (first result-lines)
        result-text (nth result-lines 1)
        expiration-date-line-pattern #"(?:\s*expire date: )(.+)(?:\R)"
        expiration-date-text (nth (re-find expiration-date-line-pattern result-text) 1)
        ;convert to date and calculate expirations days from today
        expiration-days
        (if expiration-date-text
          (try
            (.between (java.time.temporal.ChronoUnit/DAYS)
                      (java.time.LocalDate/now)
                      (parse-date-18-04 expiration-date-text))
            (catch java.time.DateTimeException ex
              (try
                (.between (java.time.temporal.ChronoUnit/DAYS)
                          (java.time.LocalDate/now)
                          (parse-date-16-04 expiration-date-text))
                (catch java.time.DateTimeException ex
                  (logging/warn "Exception parsing http certificate date (" expiration-date-text ") : " ex)
                  nil))))
          (do
            (logging/warn "No 'expire date' found in the http response below:\n" result-text)
            nil))]
    (do
      (logging/debug "result-lines:" result-lines)
      (logging/debug "result-key:" result-key)
      (logging/debug "result-text:" result-text)
      (logging/debug "expiration-date-text:" expiration-date-text)
      (if expiration-days
        {(keyword result-key) {:expiration-days expiration-days}}
        {(keyword result-key) {:expiration-days -1}}))))

(s/defn parse-http-script-responses :- HttpFactResult
  "returns a HttpFactResult from the result text of one http check"
  [raw-script-results :- s/Str]
  ()
  (apply merge
    (map parse-http-response
         (string/split raw-script-results (re-pattern output-separator)))))

(s/defn collect-http-fact
  "Collects the facts for all http checks by executing the script and parsing the results"
  [fact-config :- HttpFactConfig]
  (collect-fact
    fact-id-http
    (str
      (string/join
       "; " (map #(build-http-script %) fact-config))
      "; echo -n ''")
    :transform-fn parse-http-script-responses))

(s/defn install
  []
  (actions/package "curl"))

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

(ns dda.pallet.dda-serverspec-crate.infra.fact.command
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [clojure.string :as string]
    [dda.pallet.dda-serverspec-crate.infra.core.fact :refer :all]))

; -----------------------  fields & schemas  ------------------------
(def fact-id-command ::command)

(def CommandFactConfig {s/Keyword {:cmd s/Str}})

(def CommandFactResult {:exit-code s/Num
                        :stdout s/Str}) ;TODO:  stderr

(def CommandFactResults {s/Keyword CommandFactResult})

(def output-separator "----- command output separator -----")

; -----------------------  functions  -------------------------------
(s/defn command-to-keyword :- s/Keyword
  "creates a keyword from a bash command"
  [command :- s/Str] (keyword (string/replace command #"[:/ ]" "-")))

(defn parse-exit-code
  [command-output]
  (let [result-lines (string/split-lines command-output)
        result-key (first result-lines)
        result-out (second result-lines)
        status (int (read-string (last result-lines)))]
      {(keyword result-key) {:exit-code (if (zero? status) 1 0)
                             :stout result-out}}))

(s/defn parse-command-outputs :- CommandFactResult
  [script-result]
  ;(logging/info script-result)
  (apply merge
    (map parse-exit-code (string/split script-result (re-pattern output-separator)))))

(s/defn build-command-script
  "Builds the script for executing the commands"
  [command-config :- CommandFactConfig]
  (let [config-val (val command-config)
        config-key (key command-config)
        {:keys [cmd]} config-val]
    (str "echo '" (name config-key) "';"
         cmd "; echo $?;"
         "echo '" output-separator "'")))

(s/defn collect-command-fact
  [fact-config :- CommandFactConfig]
  (print fact-config)
  (collect-fact
    fact-id-command
    (str
      (string/join
        "; " (map #(build-command-script %) fact-config)))
    :transform-fn parse-command-outputs))

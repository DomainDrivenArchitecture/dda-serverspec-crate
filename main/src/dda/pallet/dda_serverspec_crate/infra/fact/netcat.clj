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

(ns dda.pallet.dda-serverspec-crate.infra.fact.netcat
  (:require
   [schema.core :as s]
   [pallet.actions :as actions]
   [dda.pallet.dda-serverspec-crate.infra.core.fact :refer :all]))

; -----------------------  fields & schemas  ------------------------
(def fact-id-netcat ::netcat)

(def NetcatFactConfig
  {s/Keyword
   {:host s/Str
    :port s/Num
    :timeout s/Num}})

(def NetcatFactResult {:reachable? s/Bool})

(def NetcatFactResults {s/Keyword NetcatFactResult})

(def output-separator "----- dda-pallet netcat output separator -----")

; -----------------------  functions  -------------------------------
(s/defn replace-comma :- s/Str [input :- s/Str]
  (clojure.string/replace input #"," "-"))

(s/defn ^:always-validate config-to-string :- s/Str
  [host :- s/Str port :- s/Num timeout :- s/Num]
  (str (replace-comma host)
       "_"
       port
       "_"
       timeout))

(s/defn build-netcat-script
  "builds the script from the given config"
  [netcat-config] ; :- MapEntry of NetcatFactConfig
  (let [config-val (val netcat-config)
        output-separator-with-newline (str output-separator "\n")]
    (str
     "echo '"
     (config-to-string
      (:host config-val)
      (:port config-val)
      (:timeout config-val))
     "'; nc "
     (:host config-val)
     " "
     (:port config-val)
     " -z -w "
     (:timeout config-val)
     "; echo $?"
     "; echo -n '" output-separator-with-newline "'")))

(s/defn parse-result [single-script-result :- s/Str]
  (let [result-lines (clojure.string/split single-script-result #"\n")
        result-key (first result-lines)
        result-text (last result-lines)]
    {(keyword result-key) {:reachable? (= result-text "0")}}))

(defn parse-netcat
  [script-result]
  (apply merge
    (map (comp parse-result clojure.string/trim)
      (clojure.string/split script-result (re-pattern output-separator)))))

(s/defn collect-netcat-fact
  "Collects the netcat facts."
  [netcat-configs :- NetcatFactConfig]
  (collect-fact
   fact-id-netcat
   (str
    (clojure.string/join
     "; " (map #(build-netcat-script %) netcat-configs))
    "; echo -n ''")
   :transform-fn parse-netcat))

(s/defn install
  []
  (actions/package "netcat"))

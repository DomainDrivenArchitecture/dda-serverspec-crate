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

(ns dda.pallet.dda-serverspec-crate.infra.core.fact
  (:require
    [schema.core :as s]
    [dda.pallet.commons.fact :as fact]))

(def fact-facility :dda-serverspec-fact)

(s/defn clean-up-sudo-string :- s/Str
  "Removes the sudo-prompt of a string without empty space"
  [sudo-string :- s/Str]
  (let [split-string (clojure.string/split sudo-string  #" ")]
    (if (>= (count split-string) 4) (nth split-string 4) sudo-string)))

(s/defn clean-up-fact-size-in-bytes :- s/Num ; maybe generic name?
  "Parses int size"
  [size-string :- s/Str]
  (Integer. (re-find #"\d+" size-string)))

(def collect-fact (partial fact/collect-fact fact-facility))

(def collect-exit-code-fact (partial fact/collect-exit-code-fact fact-facility))

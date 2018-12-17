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


(ns dda.pallet.dda-serverspec-crate.infra.fact.file
  (:require
    [clojure.string :as st]
    [schema.core :as s]
    [dda.pallet.dda-serverspec-crate.infra.core.fact :as fact]))

(def fact-id-file ::file)

(def FileFactConfig {s/Keyword {:path s/Str}})

(def FileFactFailResult {:path s/Str :fact-exist? (s/enum false)})

(def FileFactOkResult {:path s/Str
                     :fact-exist? (s/enum true)
                     :fact-size-in-bytes s/Num
                     :fact-user s/Str
                     :fact-group s/Str
                     :fact-mod s/Str
                     :fact-type s/Str         ; b    block (buffered) special
                                              ; c    character (unbuffered) special
                                              ; d    directory
                                              ; p    named pipe (FIFO)
                                              ; f     regular file
                                              ; l     symbolic link
                                              ; s    socket
                                              ; D   door (Solaris)
                     :fact-link-to s/Str
                     :fact-created s/Str
                     :fact-modified s/Str
                     :fact-accessed s/Str})

(def FileFactResult
  (s/either FileFactOkResult
            FileFactFailResult))

(def FileFactResults {s/Keyword FileFactResult})

(s/defn path-to-keyword :- s/Keyword
  [path :- s/Str] (keyword (st/replace path #"/" "_")))

(s/defn clean-up-negative-find :- s/Str
  "extracts path from error message"
  [negative-find :- s/Str]
  (let [split-result (st/split negative-find #" ")
        unclean-path (nth split-result 1)]
    (subs unclean-path 1 (- (count unclean-path) 2))))

(s/defn parse-find-line :- FileFactResult
  [script-result-line :- s/Str]
  (let [not-found? (not= (.indexOf script-result-line "find:") -1)
        split-string (st/split script-result-line #"'")
        path (if not-found?
               (clean-up-negative-find script-result-line)
               (nth split-string 0))]
    (if not-found?
      {:path path
       :fact-exist? false}
      (let [result-map (zipmap [:path :fact-size-in-bytes :fact-user :fact-group :fact-mod
                                :fact-type :fact-link-to :fact-created :fact-modified :fact-accessed]
                               split-string)
            cleaned-path (fact/clean-up-sudo-string (:path result-map))
            cleaned-fact-size-in-bytes (fact/clean-up-fact-size-in-bytes (:fact-size-in-bytes result-map))]
        (assoc result-map
               :path cleaned-path
               :fact-size-in-bytes cleaned-fact-size-in-bytes
               :fact-exist? true)))))

(s/defn create-line-parse-result [script-result-line]
  (let [file-fact-result (parse-find-line script-result-line)
        result-key (path-to-keyword (:path file-fact-result))]
    {result-key file-fact-result}))

(defn parse-find
  [script-result]
  (apply merge
    (map create-line-parse-result (st/split script-result #"\n"))))

(s/defn build-find-line
  "Builds the string for executing the find commands."
  [fact-config]
  (str "find " (:path (val fact-config)) " -prune -printf \"%p'%s'%u'%g'%m'%y'%l'%c'%t'%a\\n\" 2>&1"))

(s/defn collect-file-fact
  "Collects the file facts."
  [fact-configs :- FileFactConfig]
  (fact/collect-fact
    fact-id-file
    (str
      (st/join
       "; " (map #(build-find-line %) fact-configs))
      "; echo -n ''")
    :transform-fn parse-find))

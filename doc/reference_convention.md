### Convention-API
The schema for the tests is:
```clojure
(def IprouteConventionConfig (rough, exact schema has more strict dependent validations)
  (s/either {:ip ip-address-string
             (s/optional-key :via) ip-address-string
             (s/optional-key :src) ip-address-string
             (s/optional-key :dev) s/Str)}
            {:hostname s/Str
             (s/optional-key :via) ip-address-string
             (s/optional-key :src) ip-address-string})
             (s/optional-key :dev) s/Str)})

(def ServerTestConventionConfig
    {(s/optional-key :package) [{:name s/Str
                                 (s/optional-key :installed?) s/Bool}]
     (s/optional-key :netstat) [{:process-name s/Str
                                 :port s/Str
                                 (s/optional-key :running?) s/Bool
                                 (s/optional-key :ip) s/Str
                                 (s/optional-key :exp-proto) s/Str}]
     (s/optional-key :file) [{:path s/Str
                              (s/optional-key :exist?) s/Bool
                              (s/optional-key :mod) s/Str
                              (s/optional-key :user) s/Str
                              (s/optional-key :group) s/Str
                              (s/optional-key :link-to) s/Str}]
     (s/optional-key :netcat) [{:host s/Str
                                :port s/Num
                                (s/optional-key :reachable?) s/Bool}]
     (s/optional-key :certificate-file) [{:file s/Str               ;incl path as e.g. /path/file.crt
                                          :expiration-days s/Num}]  ;min days certificate must be valid
     (s/optional-key :http) [{:url s/Str                            ;full url e.g. http://google.com
                              :expiration-days s/Num}]              ;minimum days the certificate must be valid
     (s/optional-key :iproute) [IprouteConventionConfig]
     (s/optional-key :command) [{:cmd s/Str
                                 :exit-code s/Num
                                 (s/optional-key :stdout) s/Str}]})
```
The "tests.edn" file has to match this schema.  
The default value is that the test expects a positive boolean (e.g. :reachable? true) and this value can be omitted.

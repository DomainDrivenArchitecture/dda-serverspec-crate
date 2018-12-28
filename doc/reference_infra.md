### Infra-API
The infra configuration is a configuration on the infrastructure level of a crate. It contains the complete configuration options that are possible with the crate functions.
On infra level we distinguish between collecting facts (done in the settings phase without side effects) and testing (done in test phase intentionally without side effects).
Settings can also be used without tests in order to provide informations for conditional installations / configurations.

The schema is:
```clojure
(def FileFactConfig {s/Keyword {:path s/Str}})

(def NetcatFactConfig
  {s/Keyword
   {:host s/Str
    :port s/Num
    :timeout s/Num}})

(def CertificateFileFactConfig {s/Keyword {:file s/Str}})  ;with full path

(def HttpFactConfig {s/Keyword {:url s/Str}})   ;full url e.g. https://google.com

(def CommandFactConfig {s/Keyword {:cmd s/Str}})

(def CertificateFileTestConfig {s/Keyword {:expiration-days s/Num}})

(def IprouteTestConfig {s/Keyword {(s/optional-key :via) (s/pred ip?)
                                   (s/optional-key :src) (s/pred ip?)
                                   (s/optional-key :dev) s/Str
                                   :version s/Int
                                   :source s/Str}})

(def CommandTestConfig {s/Keyword {:exit-code s/Num
                                   (s/optional-key :stdout) s/Str}})

(def FileTestConfig {s/Keyword {:exist? s/Bool
                               (s/optional-key :mod) s/Str
                               (s/optional-key :user) s/Str
                               (s/optional-key :group) s/Str
                               (s/optional-key :type) s/Str
                               (s/optional-key :link-to) s/Str}})

(def HttpTestConfig {s/Keyword {:expiration-days s/Num}})

(def NetcatTestConfig {s/Keyword {:reachable? s/Bool}})

(def NetstatTestConfig {s/Keyword {:running? s/Bool
                                   :port s/Str
                                   (s/optional-key :ip) s/Str
                                   (s/optional-key :exp-proto) s/Str}})

(def PackageTestConfig {s/Keyword {:installed? s/Bool}})                                   

(def ServerTestConfig
  {(s/optional-key :package-fact) s/Any       ; parsed result of "netstat -tulpen". Any is ignored.
   (s/optional-key :netstat-fact) s/Any       ; parsed result of "dpkg -l". Any is ignored.
   (s/optional-key :file-fact)                ; parsed result of "find [path] -prune -printf \"%p'%s'%u'%g'%m'%y'%c'%t'%a\\n\"
   file-fact/FileFactConfig
   (s/optional-key :netcat-fact)              ; parsed result of "nc [host] -w [timeout] && echo $?"
   netcat-fact/NetcatFactConfig
   (s/optional-key :certificate-file-fact) certificate-file-fact/CertificateFileFactConfig
   (s/optional-key :http-fact) http-fact/HttpFactConfig
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
```
On the level of the infrastructure we break down the tests into gathering the facts and testing them against the expected value.
These results are returned in a map that follows the schema depicted above.

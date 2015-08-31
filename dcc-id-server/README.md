ICGC DCC - Identifier Server
===

Entry point to the identifier server.

Build
---

From the command line:

`cd dcc-identifier`

`mvn clean package -DskipTests -am -pl :dcc-identifier-server`

Run
---

From the command line:

`java -jar dcc-identifier-server-[VERSION].jar --spring.profiles.active=[development|production]`


ICGC DCC - ID Server
===

Entry point to the ID server.

Build
---

From the command line:

`cd dcc-identifier`

`mvn clean package -DskipTests -am -pl :dcc-id-server`

Run
---

From the command line:

`java -jar dcc-id-server-[VERSION].jar --spring.profiles.active=[development|production] --spring.config.location=src/main/conf/`


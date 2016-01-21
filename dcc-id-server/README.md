ICGC DCC - ID Server
===

The ID server creates surrogate IDs the clinical data submitted by the users. The IDs are primary keys used by the [DCC Portal](https://github.com/icgc-dcc/dcc-portal).

Requirements
---

The ID server works out of the box with [H2 database](http://www.h2database.com/html/main.html). However, it is strongly recomemded to use [Postgres database](http://www.postgresql.org/) in a production environment. 

Build
---

From the command line:

`cd dcc-identifier`

`mvn clean package -am -pl :dcc-id-server`

Run
---

From the command line:

`java -jar dcc-id-server-[VERSION].jar --spring.profiles.active=[development|production] --spring.config.location=src/main/conf/`


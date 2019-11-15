FROM maven:3.6.2-jdk-8 as builder

COPY . /srv
WORKDIR /srv
ENV JAR_FILE            /dcc-id-server.jar
RUN mvn clean package -Denforcer.skip -DskipTests \
    && cd /srv/dcc-id-server/target \
    && mv dcc-id-server*-dist.tar.gz dcc-id-server.tar.gz \
    && tar zxvf dcc-id-server.tar.gz -C /tmp \
    && mv -f /tmp/dcc-id-server-*  /tmp/dcc-id-dist  \
    && cp -f /tmp/dcc-id-dist/lib/dcc-id-server.jar $JAR_FILE

################################################################################################################

FROM openjdk:8-jdk-stretch as server

# Paths
ENV DCC_ID_SERVER_HOME /dcc-id-server
ENV DCC_ID_SERVER_LOGS $DCC_ID_SERVER_HOME/logs
ENV JAR_FILE   /dcc-id-server.jar
COPY --from=builder $JAR_FILE $JAR_FILE

WORKDIR $DCC_ID_SERVER_HOME

CMD mkdir -p  $DCC_ID_SERVER_HOME $DCC_ID_SERVER_LOGS \
    && java -Dlog.path=$DCC_ID_SERVER_LOGS \
    -jar $JAR_FILE \
    --spring.config.location=classpath:/application.yml

################################################################################################################

FROM postgres:9.6 as db

ENV SCHEMA_SQL /srv/dcc-id-server/src/main/resources/sql/schema.sql
ENV PGDATA /var/lib/postgresql/data/pgdata
ENV POSTGRES_INIT_SQL /docker-entrypoint-initdb.d/init.sql
COPY --from=builder $SCHEMA_SQL $POSTGRES_INIT_SQL

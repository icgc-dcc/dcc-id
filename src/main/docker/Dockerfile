#     ______________________   ________ 
#    /  _/ ____/ ____/ ____/  /  _/ __ \
#    / // /   / / __/ /       / // / / /
#  _/ // /___/ /_/ / /___   _/ // /_/ / 
# /___/\____/\____/\____/  /___/_____/  
                                                                                                                      
# Banner @ https://goo.gl/hnZ0GX

FROM postgres:9.2
MAINTAINER ICGC <dcc-support@icgc.org>

#
# Install Oracle JDK 8
#

RUN \
    echo "Add webupd8 repository..."  && \
    echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list  && \
    echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list  && \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886  && \
    apt-get update  && \
    \
    \
    echo "Install Java"  && \
    echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections  && \
    echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections  && \
    DEBIAN_FRONTEND=noninteractive  apt-get install -y --force-yes oracle-java8-installer oracle-java8-set-default  && \
    \
    \
    echo "Clean up..."  && \
    rm -rf /var/cache/oracle-jdk8-installer  && \
    apt-get clean  && \
    rm -rf /var/lib/apt/lists/*
 
#
# Install ID Server
#

# TODO: Update configuration to point to container PG (localhost:5432)   
ADD https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-id-server/[RELEASE]/dcc-id-server-[RELEASE]-dist.tar.gz /dcc-id.tar.gz
RUN \
  tar xf dcc-id.tar.gz && \
  rm dcc-id.tar.gz && \
  mv dcc-id-server-* dcc-id-server
 
#
# Init
#

ADD init.sql /docker-entrypoint-initdb.d/init.sql
ADD init.sh /docker-entrypoint-initdb.d/init.sh

ENV POSTGRES_DB=dcc_identifier
ENV POSTGRES_USER=dcc
ENV POSTGRES_PASSWORD=password

ENV AUTH_SERVER_URL=
ENV AUTH_SERVER_CLIENTID=
ENV AUTH_SERVER_CLIENTSECRET=

EXPOSE 8443 8444

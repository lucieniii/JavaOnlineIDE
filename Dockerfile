FROM python:3.8.3
ADD sources.list /etc/apt
RUN apt-get update && \
    apt-get install openjdk-11-jdk=11.0.7+10-3~deb10u1 nodejs=10.21.0~dfsg-1~deb10u1

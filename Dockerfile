FROM python:3.8.3
ADD sources.list /etc/apt
RUN apt-get update && \
    apt-get install -y default-jdk=2:1.11-71 nodejs=10.21.0~dfsg-1~deb10u1
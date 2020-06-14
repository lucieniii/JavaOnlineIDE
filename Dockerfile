FROM ubuntu:16.04
ADD sources.list /etc/apt
RUN apt update

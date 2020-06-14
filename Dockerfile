FROM python:3.8.3
ADD sources.list /etc/apt
RUN apt-get update

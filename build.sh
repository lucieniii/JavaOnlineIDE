#!/bin/sh
cd web && \
npm run build && \
cd .. && \
./mvnw package
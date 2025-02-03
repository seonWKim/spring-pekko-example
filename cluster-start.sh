#!/bin/bash

run_application() {
  local port=$1
  local pekko_port=$2

  ./gradlew bootRun \
    --args="--server.port=${port} \
            --pekko.remote.artery.canonical.port=${pekko_port}" \
    -PmainClass=org.github.seonwkim.springpekko.SpringPekkoApplication \
    > "log_${port}.txt" 2>&1 &
}

run_application 8080 2551
run_application 8081 2552
run_application 8082 2553

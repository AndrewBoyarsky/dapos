#!/bin/bash

{
  tendermint node --abci grpc --proxy_app tcp://127.0.0.1:26658 2>&1 &
  echo $! | tee node.pid
} | tee ~/.dapos/logs/node.log &
#!/bin/bash

while read p; do
  kill -9 $p
done <pid.txt
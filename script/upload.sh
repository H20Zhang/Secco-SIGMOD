#!/usr/bin/env bash

Cluster="XXX"

rsync -rvz --progress *.jar ./script/* ./script/upload/* ${Cluster}
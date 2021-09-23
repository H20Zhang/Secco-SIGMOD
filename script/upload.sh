#!/usr/bin/env bash

Cluster="itsc:/users/itsc/s880006/secco-sigmod"

rsync -rvzp --progress *.jar ./script/* ./script/upload/* ${Cluster}

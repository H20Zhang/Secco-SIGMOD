#!/usr/bin/env bash

Cluster="itsc:/users/XXX/s880006/secco-sigmod"

rsync -rvzp --progress *.jar ./script/* ./script/upload/* ${Cluster}

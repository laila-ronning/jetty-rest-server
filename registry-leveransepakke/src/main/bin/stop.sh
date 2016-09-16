#!/bin/bash

DIR=$( cd $( dirname $0 ) && pwd )

if [ -f "$DIR/registry.pid" ] ; then
    echo "Stopper registry-server med pid `cat $DIR/registry.pid` - $(date)"
    kill `cat $DIR/registry.pid`
    while ps -p `cat $DIR/registry.pid` > /dev/null; do sleep 1; done
    echo "Registry-server stoppet - $(date)"
fi

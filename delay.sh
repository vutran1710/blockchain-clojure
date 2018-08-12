#!/bin/bash
# delay.sh

set -e

cmd="$@"

until curl $BOOT_NODE; do
    >&2 echo "Boot is unavailable - sleeping"
    sleep 5
done

>&2 echo "Boot is up - executing command"
exec $cmd

#!/bin/bash
# delay.sh

set -e

host="$1"
shift
cmd="$@"

until curl 172.16.238.10; do
    >&2 echo "Boot is unavailable - sleeping"
    sleep 10
done

>&2 echo "Boot is up - executing command"
exec $cmd

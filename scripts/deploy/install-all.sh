#!/bin/bash

set -e
set -x

. "$(dirname "${BASH_SOURCE[0]}")/../lib/common.sh"

. "$SCRIPTS_DIR/deploy/install-db.sh"

. "$SCRIPTS_DIR/deploy/build-app.sh"

. "$SCRIPTS_DIR/deploy/install-app.sh"

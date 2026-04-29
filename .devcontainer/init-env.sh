#!/bin/bash

set -e

mkdir -p assets

if [ ! -f assets/env.dev ]; then
  echo "Creating assets/env.dev ..."

  cat > assets/env.dev <<EOF
DB_HOST=db
DB_PORT=5432
DB_NAME=central_db
DB_USER=postgres
DB_PASSWORD=Giorgos13

JWT_SECRET=dev-secret
JWT_ISSUER=ktor-app
JWT_AUDIENCE=ktor-client
EOF

else
  echo "assets/env.dev already exists"
fi
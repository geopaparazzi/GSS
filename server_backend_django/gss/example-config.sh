# if you want debug active
export DEBUG=true
# logging level
export DJANGO_LOG_LEVEL=DEBUG

# database connection parameters
export POSTGRES_USER=
export POSTGRES_PASSWORD=
export POSTGRES_DB=gssdb
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5433

# deploy: IMPORTANT: make sure that you add your server name/address
export ALLOWED_HOSTS=".localhost 0.0.0.0 127.0.0.1 [::1]"
export CORS_ALLOWED_ORIGINS="http://localhost:80 http://localhost:8080"
export CSRF_TRUSTED_ORIGINS="http://localhost:80 http://localhost:8080"

export SECRETKEY=

# frontend imagegrid rendering in readonly notes (true=show all, false=show only selected)
export GSS_IMAGEGRID_SHOW_ALL=false
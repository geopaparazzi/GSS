[![DOI](https://zenodo.org/badge/280122006.svg)](https://zenodo.org/badge/latestdoi/280122006)

# Docker

## Development

```
cd docker
docker compose --profile dev up
```

## Production

Add your domain in `ALLOWED_HOSTS` for the production `django` container, in `docker/docker-compose.yml`, then:

```
cd docker
docker compose --profile prod up -d
```

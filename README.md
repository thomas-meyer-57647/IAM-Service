# iam-service

## Service Name

`spring.application.name=iam-service`

## Environment Variables

Der Service verwendet service-spezifische Environment-Variablen:

- `IAM_DB_HOST`
- `IAM_DB_PORT`
- `IAM_DB_NAME`
- `IAM_DB_USER`
- `IAM_DB_PASSWORD`
- `IAMPORT`

## IntelliJ Run Configuration

Beispiel fuer `Environment variables`:

`IAM_DB_HOST=localhost;IAM_DB_PORT=3306;IAM_DB_NAME=iam;IAM_DB_USER=root;IAM_DB_PASSWORD=;IAMPORT=8080`

Hinweis: `Include system environment variables` kann aktiv bleiben, da die Variablen service-spezifisch sind.

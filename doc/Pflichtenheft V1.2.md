# Pflichtenheft - iam-service (i18n) - Version 1.2 (Minor)

## Metadaten
- **Service-Name:** iam-service
- **Version:** 1.2 (Minor)
- **Stand:** 25.02.2026
- **Ziel:** Konsolidierung des JWT Contracts gemäß auth-service v1.1 und Einführung eines klaren Tenant-zu-projectKey Mapping Modes für i18n.
- **Geltungsbereich:** i18n Service: Projekte/Sprachen/Bundles, Runtime-Translations. Service-Name bleibt aus Kompatibilitätsgründen 'iam-service'.

## 0. Änderungshistorie (gegenüber V1.1)
- JWT Contract v1.1 ist MUSS: scp Array + subject_type + token_version optional.
- Tenant-zu-projectKey Mapping wird als konfigurierbarer Mode definiert (STRICT vs DECOUPLED).
- Standardisierte Request-Konventionen (X-Correlation-Id) und Fehlercodes vereinheitlicht.

## 1. Ziele
- i18n Daten sind mandantensicher isoliert (tenant_id).
- Klarer, dokumentierter Umgang mit projectKey im Pfad, ohne Cross-Tenant Leaks.
- Kompatibilität zu bestehenden Clients bleibt erhalten (Default = STRICT).

## 2. Security (MUSS)
- Resource Server: JWT validieren (Issuer/JWKS, exp).
- Audience strict: aud muss "iam-service" enthalten.
- Pflicht-Claims: iss, sub, aud, exp, iat, jti, tenant_id, subject_type, scp.
Scopes:
- i18n:read für Read/Runtime
- i18n:admin für Admin/Upload/Rollback/Purge

## 3. Tenant Mapping Modes (neu in V1.2)
Konfiguration:
- iam.tenant-mapping.mode = STRICT | DECOUPLED (Default: STRICT)
Mode STRICT (Default, kompatibel zu V1.1):
- projectKey im Pfad muss tenant_id entsprechen (projectKey == tenant_id), sonst 403.
Mode DECOUPLED (neu):
- projectKey ist ein logischer Projektschlüssel innerhalb eines Tenants.
- Alle Tabellen tragen tenant_id zusätzlich zu project_key.
- Zugriff ist erlaubt, wenn Datensatz.tenant_id == token.tenant_id.
- Projektanlage: projectKey wird innerhalb tenant_id eindeutig (UNIQUE(tenant_id, project_key)).

## 4. API (V1.2)
Die API bleibt unter /api/v1/*.
Ergänzungen/Schärfungen:
- Bei Mode DECOUPLED sind alle Routen weiterhin /api/v1/{projectKey}/..., aber die Mandantenisolation erfolgt über tenant_id in der DB, nicht über projectKey==tenant_id.
- Error Codes:
  - TENANT_MISMATCH (403)
  - PROJECT_NOT_FOUND (404) (No leak Policy je nach Mode)

## 5. Abnahmekriterien
- Tokens werden mit aud strict und scp Array akzeptiert; subject_type wird ausgewertet.
- Mode STRICT: projectKey mismatch -> 403.
- Mode DECOUPLED: zwei Tenants dürfen den gleichen projectKey verwenden, ohne Daten zu sehen.
- OpenAPI dokumentiert Mapping-Mode und Security-Anforderungen.

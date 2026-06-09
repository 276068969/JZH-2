# Prison Management Platform

This project uses a fixed local port plan, Docker-based deployment, domestic dependency mirrors, and Docker layer caching for repeatable builds without VPN.

## Stack

- Frontend: Vue 3 + Vite + TypeScript + Element Plus
- Backend: Spring Boot 3 + Maven + Java 17
- Database: MySQL 8
- Middleware: Redis 7
- Static hosting: Nginx

## Fixed Port Table

All host ports are defined in the root `.env` file and exposed only on IPv4 loopback.

| Service | Host address | Host port | Container port |
|---|---|---:|---:|
| Frontend | `127.0.0.1` | `3008` | `80` |
| Backend | `127.0.0.1` | `8088` | `8080` |
| MySQL | `127.0.0.1` | `3309` | `3306` |
| Redis | `127.0.0.1` | `6380` | `6379` |

The project does not auto-switch ports. If one of these ports is occupied, the startup script fails fast and reports the owning process.

## Environment File

The root `.env` file is the single source of truth for:

- `DOCKER_REGISTRY`
- `NPM_REGISTRY`
- `MAVEN_MIRROR_URL`
- `FRONTEND_PORT`
- `BACKEND_PORT`
- `MYSQL_PORT`
- `REDIS_PORT`
- `MYSQL_ROOT_PASSWORD`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`

## Domestic Mirrors

The Docker build now uses these mirror controls:

- Base images: `${DOCKER_REGISTRY}`
- npm packages: `${NPM_REGISTRY}`
- Maven dependencies: `${MAVEN_MIRROR_URL}`

No `# syntax=docker/dockerfile:*` directive is used.

## Docker Cache Strategy

The Dockerfiles are arranged so dependency downloads stay cached unless dependency manifests change.

Frontend cache boundary:

- `package.json`
- `package-lock.json`

Backend cache boundary:

- `pom.xml`
- `maven-settings.xml`

Effect:

- First build downloads dependencies.
- Re-running `docker compose up --build -d` reuses cached dependency layers when manifests are unchanged.
- Editing only business source code recompiles the project without re-downloading npm or Maven dependencies.

## Start

Use the provided PowerShell script:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\docker-up.ps1
```

What the script does:

1. Loads the root `.env`.
2. Verifies the fixed ports are free.
3. Runs `docker compose up --build -d`.
4. Waits for both `http://127.0.0.1:<FRONTEND_PORT>` and `http://localhost:<FRONTEND_PORT>`.
5. Compares their HTML head output and fails if they are different.
6. Prints the frontend access address.

## Manual Commands

If you want to run the checks manually:

```powershell
docker compose up --build -d
Get-NetTCPConnection -State Listen -LocalPort 3008,8088,3309,6380 | Select-Object LocalAddress,LocalPort,OwningProcess
curl.exe -sS http://127.0.0.1:3008 | Select-Object -First 20
curl.exe -sS http://localhost:3008 | Select-Object -First 20
```

On systems with WSL and `lsof` available:

```powershell
wsl.exe bash -lc "lsof -nP -iTCP:3008 -sTCP:LISTEN"
wsl.exe bash -lc "lsof -nP -iTCP:8088 -sTCP:LISTEN"
wsl.exe bash -lc "lsof -nP -iTCP:3309 -sTCP:LISTEN"
wsl.exe bash -lc "lsof -nP -iTCP:6380 -sTCP:LISTEN"
```

## Frontend Dev Server Rules

`frontend/vite.config.ts` now enforces:

- `server.host = "127.0.0.1"`
- `server.port = FRONTEND_PORT`
- `server.strictPort = true`
- `preview.host = "127.0.0.1"`
- `preview.port = FRONTEND_PORT`
- `preview.strictPort = true`

The frontend proxy targets `http://127.0.0.1:${BACKEND_PORT}`.

## Test Accounts

The backend initializes passwords for these accounts at startup:

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `Admin@123456` |
| Manager | `manager` | `Manager@123456` |
| Guard | `guard` | `Guard@123456` |
| Doctor | `doctor` | `Doctor@123456` |
| Viewer | `viewer` | `Viewer@123456` |

## Access Address

After a successful build, open:

```text
http://localhost:3008
```

The script also verifies that `http://127.0.0.1:3008` returns the same page.

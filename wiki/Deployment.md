# Deployment

## Overview

StudyHub is deployed as a set of Docker containers orchestrated with Docker Compose. The app is live at
https://studyhub.college with the API at https://api.studyhub.college.

Pushing to `main` automatically builds and deploys via GitHub Actions.

---

## Running Locally

**Prerequisites:** Docker Desktop, Git

```bash
git clone https://github.com/DaraghRoche7/CS4135-Social-Networking-Platform.git
cd CS4135-Social-Networking-Platform

cp .env.example .env
# Edit .env and set JWT_SECRET, DB_PASSWORD and INTERNAL_API_KEY

docker compose -f docker-compose.prod.yml up -d --build
```

Frontend: http://localhost:3000  
API Gateway: http://localhost:8080

Verify everything is healthy:
```bash
docker compose -f docker-compose.prod.yml ps
```

Stop the stack:
```bash
docker compose -f docker-compose.prod.yml down
```

---

## Production

The app runs on an Oracle Cloud Ubuntu 22.04 server.

NGINX proxies public traffic to the containers and HTTPS is handled by Let's Encrypt.

Deployment is fully automated. Pushing to `main` triggers the GitHub Actions pipeline which builds all Docker images, pushes them to Docker Hub, and restarts the containers on the server.
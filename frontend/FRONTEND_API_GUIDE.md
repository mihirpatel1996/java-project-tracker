# Frontend API Configuration Guide

This document explains how API requests work in different environments and why.

---

## Table of Contents

1. [Quick Reference](#quick-reference)
2. [The Three Environments](#the-three-environments)
3. [How Each Environment Works](#how-each-environment-works)
4. [Why This Setup?](#why-this-setup)
5. [Files Changed](#files-changed)
6. [Running the App](#running-the-app)
7. [Troubleshooting](#troubleshooting)

---

## Quick Reference

| Environment | Command | API URL | CORS Needed? |
|-------------|---------|---------|--------------|
| Local Dev (no Docker) | `npm run dev` | `http://localhost:8080` | ✅ Yes |
| Docker (local) | `docker compose up` | `` (empty/relative) | ❌ No |
| Production (AWS) | `docker compose up` | `` (empty/relative) | ❌ No |

---

## The Three Environments

### Environment 1: Local Development (Without Docker)

```
You run:
  Terminal 1: cd backend && ./mvnw spring-boot:run     (port 8080)
  Terminal 2: cd frontend && npm run dev               (port 5173)

Browser: http://localhost:5173
```

### Environment 2: Docker (Local Machine)

```
You run:
  docker compose --profile local up --build

Browser: http://localhost (port 80)
```

### Environment 3: Production (AWS EC2)

```
Server runs:
  docker compose -f docker-compose.prod.yml up -d

Browser: http://your-ec2-ip (port 80)
```

---

## How Each Environment Works

### Environment 1: Local Development (Without Docker)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                  LOCAL DEVELOPMENT (npm run dev)                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   .env.development loaded by Vite:                                       │
│   VITE_API_URL=http://localhost:8080                                     │
│                                                                          │
│   ┌──────────────────┐                    ┌──────────────────┐           │
│   │     Browser      │                    │     Backend      │           │
│   │                  │                    │                  │           │
│   │  localhost:5173  │                    │  localhost:8080  │           │
│   │                  │                    │                  │           │
│   │  React App       │  POST /api/auth/   │  Spring Boot     │           │
│   │  (Vite Dev       │  ─────────────────►│                  │           │
│   │   Server)        │  register          │  Checks CORS:    │           │
│   │                  │                    │  "Is :5173       │           │
│   │                  │  ◄─────────────────│   allowed?"      │           │
│   │                  │  Response          │                  │           │
│   └──────────────────┘                    └──────────────────┘           │
│                                                                          │
│   ⚠️  Different ports = Different origins = CORS check required          │
│                                                                          │
│   Backend SecurityConfig.java must allow http://localhost:5173           │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

**Why CORS is needed:**
- Browser is on port 5173
- API is on port 8080
- Different ports = different origins
- Browser checks: "Is 5173 allowed to call 8080?"
- Backend must respond: "Yes, 5173 is allowed"


### Environment 2: Docker (Local Machine)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     DOCKER (localhost:80)                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   .env.production loaded during build:                                   │
│   VITE_API_URL=   (empty)                                                │
│                                                                          │
│   ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐  │
│   │     Browser      │    │      Nginx       │    │     Backend      │  │
│   │                  │    │                  │    │                  │  │
│   │  localhost:80    │    │  localhost:80    │    │  backend:8080    │  │
│   │                  │    │  (container)     │    │  (container)     │  │
│   │                  │    │                  │    │                  │  │
│   │  POST /api/auth/ │    │  Receives        │    │                  │  │
│   │  ───────────────►│    │  request         │    │                  │  │
│   │  register        │    │                  │    │                  │  │
│   │                  │    │  Sees /api/*     │    │                  │  │
│   │  Same origin!    │    │  ───────────────►│    │  Processes       │  │
│   │  No CORS check!  │    │  Forwards to     │    │  request         │  │
│   │                  │    │  backend:8080    │    │                  │  │
│   │                  │    │                  │    │                  │  │
│   │                  │    │  ◄───────────────│    │  Returns         │  │
│   │  ◄───────────────│    │  Gets response   │    │  response        │  │
│   │  Gets response   │    │                  │    │                  │  │
│   └──────────────────┘    └──────────────────┘    └──────────────────┘  │
│                                                                          │
│   ✅ Browser only talks to port 80                                       │
│   ✅ Same origin = No CORS check                                         │
│   ✅ Nginx secretly forwards to backend                                  │
│   ✅ Browser never knows backend exists                                  │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

**Why CORS is NOT needed:**
- Browser loads page from `localhost:80`
- Browser sends API request to `localhost:80/api/*`
- Same host, same port = same origin
- Browser doesn't check CORS for same-origin requests
- Nginx internally forwards to backend (browser doesn't know)


### Environment 3: Production (AWS EC2)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                  PRODUCTION (AWS EC2)                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   Exactly same as Docker local, just on a server:                        │
│                                                                          │
│   ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐  │
│   │   User Browser   │    │      Nginx       │    │     Backend      │  │
│   │                  │    │                  │    │                  │  │
│   │  http://your-    │    │  EC2:80          │    │  EC2:8080        │  │
│   │  ec2-ip.com      │    │  (container)     │    │  (container)     │  │
│   │                  │    │                  │    │                  │  │
│   │                  │    │                  │    │  ┌────────────┐  │  │
│   │  POST /api/auth/ │    │                  │    │  │            │  │  │
│   │  ───────────────►│    │  ───────────────►│    │  │    RDS     │  │  │
│   │  register        │    │                  │    │  │   MySQL    │  │  │
│   │                  │    │                  │    │  │            │  │  │
│   │  ◄───────────────│    │  ◄───────────────│    │  └────────────┘  │  │
│   │                  │    │                  │    │                  │  │
│   └──────────────────┘    └──────────────────┘    └──────────────────┘  │
│                                                                          │
│   ✅ Same concept as Docker local                                        │
│   ✅ Nginx proxies API requests                                          │
│   ✅ No CORS issues                                                      │
│   ✅ Backend connects to RDS instead of local MySQL                      │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Why This Setup?

### The Problem with Hardcoded URLs

```javascript
// ❌ BAD - Only works in one environment
const API_BASE_URL = 'http://localhost:8080';
```

| Environment | Works? | Why |
|-------------|--------|-----|
| Local dev | ✅ | Ports match |
| Docker | ❌ | CORS blocks localhost:80 → localhost:8080 |
| Production | ❌ | localhost doesn't exist on server |

### The Solution: Environment-Based URLs

```javascript
// ✅ GOOD - Works in all environments
const API_BASE_URL = import.meta.env.VITE_API_URL || '';
```

| Environment | VITE_API_URL | Result |
|-------------|--------------|--------|
| Local dev | `http://localhost:8080` | Direct call (CORS handled by backend) |
| Docker | `` (empty) | Relative URL `/api/*` (Nginx proxies) |
| Production | `` (empty) | Relative URL `/api/*` (Nginx proxies) |

---

## Files Changed

### 1. Create `frontend/.env.development`

```bash
# Used when running: npm run dev
VITE_API_URL=http://localhost:8080
```

### 2. Create `frontend/.env.production`

```bash
# Used when running: npm run build (Docker uses this)
VITE_API_URL=
```

### 3. Update `frontend/src/services/authService.js`

Change this line:
```javascript
// Before
const API_BASE_URL = 'http://localhost:8080';

// After
const API_BASE_URL = import.meta.env.VITE_API_URL || '';
```

### 4. Update `frontend/src/services/projectService.js`

Same change:
```javascript
// Before
const API_BASE_URL = 'http://localhost:8080';

// After
const API_BASE_URL = import.meta.env.VITE_API_URL || '';
```

### 5. Backend CORS (Already configured)

Your `SecurityConfig.java` should allow `http://localhost:5173` for local development.
In Docker/Production, CORS doesn't apply because Nginx makes everything same-origin.

---

## Running the App

### Option 1: Local Development (No Docker)

```bash
# Terminal 1 - Start Backend
cd backend
./mvnw spring-boot:run

# Terminal 2 - Start Frontend
cd frontend
npm run dev

# Open browser
http://localhost:5173
```

### Option 2: Docker (Local)

```bash
# Make sure local MySQL is stopped (if running)
net stop mysql  # Windows
# or
sudo service mysql stop  # Linux/Mac

# Start everything
docker compose --profile local up --build

# Open browser
http://localhost
```

### Option 3: Production (AWS)

```bash
# On EC2 server
cd ~/app
docker compose -f docker-compose.prod.yml up -d

# Open browser
http://your-ec2-ip
```

---

## Troubleshooting

### CORS Error in Docker

**Symptom:**
```
Access to XMLHttpRequest at 'http://localhost:8080/api/...' 
from origin 'http://localhost' has been blocked by CORS policy
```

**Cause:** Frontend is calling `localhost:8080` directly instead of using relative URLs.

**Fix:** Make sure:
1. `.env.production` has `VITE_API_URL=` (empty)
2. Service files use `import.meta.env.VITE_API_URL || ''`
3. Rebuild: `docker compose --profile local up --build`


### API Returns 404 in Docker

**Symptom:** API calls return 404 Not Found

**Cause:** Nginx not forwarding to backend correctly.

**Check:**
1. Backend container is running: `docker compose ps`
2. Nginx config has correct proxy_pass: `proxy_pass http://backend:8080/api/`
3. Backend logs: `docker compose logs backend`


### Works Locally, Fails in Docker

**Symptom:** `npm run dev` works, but Docker version doesn't.

**Cause:** Wrong environment file loaded or old build cached.

**Fix:**
```bash
# Force rebuild without cache
docker compose --profile local down
docker compose --profile local build --no-cache
docker compose --profile local up
```


### Cannot Connect to Database in Docker

**Symptom:** Backend fails with database connection error.

**Check:**
1. MySQL container is running: `docker compose ps`
2. MySQL is healthy: `docker compose logs mysql`
3. `.env` has correct DB_URL: `jdbc:mysql://mysql:3306/projecttracker`

Note: In Docker, use `mysql` (container name) not `localhost`!

---

## Visual Summary

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                          │
│   LOCAL DEV (npm run dev)                                                │
│   ═══════════════════════                                                │
│                                                                          │
│   Browser:5173  ──────────────────────────────────►  Backend:8080        │
│                       CORS required                                      │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   DOCKER / PRODUCTION                                                    │
│   ═══════════════════                                                    │
│                                                                          │
│   Browser:80  ───────►  Nginx:80  ───────►  Backend:8080                 │
│                  │                    │                                  │
│                  │                    └── Internal (browser doesn't see) │
│                  │                                                       │
│                  └── Same origin (no CORS needed)                        │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Key Takeaways

1. **CORS is a browser security feature** - It only applies when browser calls a different origin.

2. **Nginx acts as a reverse proxy** - It receives all requests and forwards API calls to backend internally.

3. **Same origin = No CORS** - When browser and API appear to be on same host:port, no CORS check happens.

4. **Environment variables control behavior** - `.env.development` for local, `.env.production` for Docker/prod.

5. **Vite's `import.meta.env`** - This is how you access environment variables in Vite/React apps.

6. **The empty string trick** - `VITE_API_URL=` means use relative URLs like `/api/auth/register` instead of absolute URLs.

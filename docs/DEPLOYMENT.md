# Deployment & Demonstration Guide

## 1. Architectural Clarification: Netlify vs. Java Spring Boot

### Why Java Spring Boot Cannot Run Directly on Netlify
- **Netlify's Architecture**: Netlify is a Global CDN built specifically for **JAMstack (static HTML, CSS, client-side JavaScript)** and serverless functions (Node.js, Go, Rust running on AWS Lambda).
- **SentinelTrace's Architecture**: SentinelTrace is an enterprise **Java 21 Spring Boot 3.3.4** backend with a persistent JVM daemon, embedded Apache Tomcat web server, Spring Data JPA, and an active **PostgreSQL** relational database.
- **Incompatibility**: Netlify does not provide a Java Virtual Machine (JVM) runtime, cannot execute executable `.jar` files, does not maintain long-running server sockets, and cannot host or bundle PostgreSQL databases.

---

## 2. Active Option: Instant Live Public HTTPS Tunnel (Active)

To share the running SentinelTrace application with professors, evaluators, or team members immediately, a secure HTTPS tunnel is active:

- **Public Dashboard URL**: [https://sentineltrace-live.loca.lt/dashboard](https://sentineltrace-live.loca.lt/dashboard)
- **Public REST API Base**: `https://sentineltrace-live.loca.lt/api`
- **First-Time Browser Access Password**: `223.181.61.198` (localtunnel requires entering the host's public IP once in the browser friendly landing page to establish trust).

### How to Start/Restart the Tunnel Locally:
```powershell
npx -y localtunnel --port 8081 --subdomain sentineltrace-live
```

---

## 3. Cloud Deployment: Render / Railway / Docker (Containerized)

For a fully cloud-hosted deployment that stays online independently of your local machine:

### A. Deploying via Render.com (Free Tier)
1. Push this repository to GitHub.
2. Log in to [Render.com](https://render.com).
3. Click **New +** -> **Blueprint**.
4. Connect your GitHub repository.
5. Render reads `render.yaml` automatically, provisions:
   - A managed PostgreSQL database (`sentineltrace-db`)
   - A Docker container running Java 21 OpenJDK and `target/sentineltrace-1.0.0.jar`
6. Click **Apply**. Render will build and deploy the project to a public `https://sentineltrace-app.onrender.com` URL.

### B. Building and Running with Docker Locally:
```bash
# Build the production container
docker build -t sentineltrace:1.0.0 .

# Run container linked to host PostgreSQL
docker run -p 8081:8081 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/sentineltrace \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=your_password \
  sentineltrace:1.0.0
```

---

## 4. Local Viva / College Presentation Setup

During an in-person college viva or lab evaluation:
1. Ensure PostgreSQL is running (`net start postgresql-x64-18`).
2. Run SentinelTrace via JAR:
   ```powershell
   java -jar target\sentineltrace-1.0.0.jar --server.port=8081
   ```
3. Open your browser at `http://localhost:8081/dashboard`.
4. Demonstrate detection rules using the built-in quick simulation button or PowerShell:
   ```powershell
   Invoke-RestMethod -Uri http://localhost:8081/api/demo/simulate -Method POST -Body '{"scenario":"FAILED_LOGINS","eventCount":5}' -ContentType 'application/json'
   ```

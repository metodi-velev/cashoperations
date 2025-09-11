# Prerequisites:
# - Docker Desktop installed and running
# - This command sequence is intended to be run in a terminal from the project root
# - Port 8080 on your host should be free

# 1) (Optional) Clean previous container if it exists
# Ignore errors if it wasn’t running
docker stop cashoperations || true
docker rm cashoperations || true

# 2) Build the Docker image (uses the multi-stage Dockerfile in this repo)
# The image will be tagged as cashoperations:latest
docker build -t cashoperations:latest .

# 3) Run the container mapping host port 8080 to container port 8080
# You can override Spring property `fib.auth.api-key` using the ENV var FIB_AUTH_API_KEY if needed
# Example: replace <your-api-key> (or omit the env line to use the default in application.yml)
# Windows PowerShell example:
# docker run --name cashoperations --rm -p 8080:8080 -e FIB_AUTH_API_KEY=<your-api-key> cashoperations:latest

# Minimal run (no env override, uses application.yml default):
docker run --name cashoperations --rm -p 8080:8080 cashoperations:latest

# 4) Check logs (in another terminal) or use -it to attach directly
# docker logs -f cashoperations

# 5) Test the API (example)
# Replace <API_KEY> if you set a custom env value when starting the container
# Windows PowerShell Invoke-WebRequest example:
# Invoke-WebRequest -Method POST -Uri http://localhost:8080/api/v1/cash-operation -Headers @{"FIB-X-AUTH"="<API_KEY>"} -ContentType 'application/json' -Body '{"cashierName":"LINDA","currency":"EUR","operationType":"DEPOSIT","amount":200.00,"denominations":[{"quantity":2,"value":50},{"quantity":1,"value":100}]}'

# 6) Stop the container when you’re done
docker stop cashoperations

# --- Using Docker Compose instead (recommended) ---
# Build and start in detached mode:
# docker compose up -d --build
# Check logs:
# docker compose logs -f
# Stop and remove containers:
# docker compose down

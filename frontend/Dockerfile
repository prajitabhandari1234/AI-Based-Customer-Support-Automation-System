FROM node:22-alpine AS build
WORKDIR /app

# Install exactly the dependency versions recorded in package-lock.json.
COPY package.json package-lock.json ./
RUN npm ci

# Vite values are build-time settings because they are embedded into the browser bundle.
ARG VITE_API_BASE_URL=""
ARG VITE_API_TIMEOUT_MS=30000
ARG VITE_ENABLE_DEMO_ACCOUNTS=false
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL} \
    VITE_API_TIMEOUT_MS=${VITE_API_TIMEOUT_MS} \
    VITE_ENABLE_DEMO_ACCOUNTS=${VITE_ENABLE_DEMO_ACCOUNTS}

COPY . .
RUN npm run build

FROM nginx:1.27-alpine

# The official Nginx image substitutes environment variables in templates at container startup.
ENV BACKEND_API_URL=http://backend:8080
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1/ || exit 1

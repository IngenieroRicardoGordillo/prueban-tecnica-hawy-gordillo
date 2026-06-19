FROM node:20-alpine AS builder
WORKDIR /app

# Build args para inyectar variables en build time
ARG VITE_PRODUCTS_API_URL
ARG VITE_INVENTORY_API_URL
ARG VITE_PRODUCTS_API_KEY
ARG VITE_INVENTORY_API_KEY

ENV VITE_PRODUCTS_API_URL=$VITE_PRODUCTS_API_URL
ENV VITE_INVENTORY_API_URL=$VITE_INVENTORY_API_URL
ENV VITE_PRODUCTS_API_KEY=$VITE_PRODUCTS_API_KEY
ENV VITE_INVENTORY_API_KEY=$VITE_INVENTORY_API_KEY

COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.25-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

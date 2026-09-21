# Étape 1 : compiler le frontend Angular 19.
FROM node:22-alpine AS build

WORKDIR /app

# Installer les dépendances à partir du fichier de verrouillage.
COPY package.json package-lock.json ./
RUN npm ci

# Copier les sources et compiler en mode production.
COPY . .
RUN npm run build -- --configuration production

# Étape 2 : servir les fichiers Angular avec Nginx.
FROM nginx:stable-alpine

# La configuration Nginx sera créée à l'étape suivante.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Angular place les fichiers destinés au navigateur dans "browser".
COPY --from=build /app/dist/my-popote-front/browser/ /usr/share/nginx/html/

EXPOSE 80

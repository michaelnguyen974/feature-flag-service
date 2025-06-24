#!/bin/bash

echo "🚩 Setting up Flagd Controller..."

mkdir -p flagd-controller/{backend,frontend}
cd src/flagd-controller

echo "📁 Creating directory structure..."

echo "📄 Creating flags.flagd.json..."
cat > /Users/michaelnguyen/Downloads/open-feature-demo2-real/src/main/java/com/example/open_feature_demo2_real/flags.flagd.json << 'EOF'
{
  "flags": {
    "welcome-message": {
      "variants": {
        "on": true,
        "off": false
      },
      "state": "ENABLED",
      "defaultVariant": "off"
    },
    "new-ui": {
      "variants": {
        "on": true,
        "off": false
      },
      "state": "ENABLED",
      "defaultVariant": "off"
    },
    "beta-feature": {
      "variants": {
        "on": true,
        "off": false
      },
      "state": "ENABLED",
      "defaultVariant": "off"
    }
  }
}
EOF

echo "🔧 Creating backend Dockerfile..."
cat > backend/Dockerfile << 'EOF'
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/flagd-controller-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

echo "🔧 Creating frontend Dockerfile..."
cat > frontend/Dockerfile << 'EOF'
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
FROM nginx:alpine
COPY --from=0 /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
EOF

echo "🔧 Creating nginx config..."
cat > frontend/nginx.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    server {
        listen 3000;
        location / {
            root /usr/share/nginx/html;
            index index.html;
            try_files $uri $uri/ /index.html;
        }

        location /api/ {
            proxy_pass http://flagd-controller:8081;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
    }
}
EOF

echo "📦 Creating React app structure..."
mkdir -p frontend/src

cat > frontend/src/main.jsx << 'EOF'
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
EOF

cat > frontend/src/App.jsx << 'EOF'
import FlagdController from './FlagdController'

function App() {
  return <FlagdController />
}

export default App
EOF

cat > frontend/src/index.css << 'EOF'
@tailwind base;
@tailwind components;
@tailwind utilities;

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
EOF

cat > frontend/index.html << 'EOF'
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Flagd Controller</title>
    <script src="https://cdn.tailwindcss.com"></script>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.jsx"></script>
  </body>
</html>
EOF

echo "🏗️ Building and starting services..."

echo "📦 Building backend..."
cd backend
mvn clean package -DskipTests
cd ..

echo "📦 Installing frontend dependencies..."
cd frontend
npm install
cd ..

echo "🐳 Starting with Docker Compose..."
docker-compose up --build -d

echo "✅ Setup complete!"
echo ""
echo "🌐 Services running at:"
echo "   Frontend: http://localhost:3000"
echo "   Backend API: http://localhost:8081/api/flagd"
echo "   Flagd: http://localhost:8013"
echo ""
echo "🛠️ To stop: docker-compose down"
echo "📝 Edit flags.flagd.json to add more flags"
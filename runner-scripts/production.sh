fuser -k 10500/tcp || true

source production/.env

java -jar production/libs/gardeners-grove-3.0.0.jar \
    --server.port=10500 \
    --server.servlet.contextPath=/prod \
    --spring.application.name=gardeners-grove \
    --spring.profiles.active=production

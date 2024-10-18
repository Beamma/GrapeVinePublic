fuser -k 9500/tcp || true

source staging/.env

java -jar staging/libs/gardeners-grove-3.0.0.jar \
    --server.port=9500 \
    --server.servlet.contextPath=/test \
    --spring.application.name=gardeners-grove \
    --spring.profiles.active=staging

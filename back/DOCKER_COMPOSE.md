# Docker Compose Local Development Guide

## Quick Start

### Start all services (PostgreSQL, Redis, Kafka, Zookeeper):
```bash
docker-compose up -d
```

### Stop all services:
```bash
docker-compose down
```

### View logs:
```bash
docker-compose logs -f

# Specific service
docker-compose logs -f postgres
docker-compose logs -f kafka
docker-compose logs -f redis
```

### Verify services are healthy:
```bash
docker-compose ps
```

---

## Service Details

### PostgreSQL (Port 5432)
- **Container**: recadero-postgres
- **Database**: recadero
- **User**: recadero
- **Password**: recadero123
- **Initialization**: Automatically creates 7 schemas (catalog, users, cart, orders, promotions, templates, shared)
- **Data Volume**: postgres_data (persisted between restarts)

**Connect from CLI:**
```bash
psql -h localhost -U recadero -d recadero
```

**Test connection from Spring Boot:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/recadero
spring.datasource.username=recadero
spring.datasource.password=recadero123
```

---

### Redis (Port 6379)
- **Container**: recadero-redis
- **Password**: redis123
- **Data Volume**: redis_data (persisted)
- **TTL Strategy**: Products (1 hour), Sessions (6 hours), Cart (session duration)

**Connect from CLI:**
```bash
redis-cli -h localhost -p 6379 -a redis123

# Test connection
PING  # Should respond with PONG
```

**Test connection from Spring Boot:**
```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=redis123
sSpring.redis.timeout=2000ms
```

---

### Kafka (Port 9092)
- **Container**: recadero-kafka
- **Bootstrap Servers**: localhost:9092 (external), kafka:29092 (internal)
- **Data Volume**: kafka_data (persisted)
- **Auto-create Topics**: Enabled (topics created on first publish)
- **Log Retention**: 24 hours
- **Replication Factor**: 1 (appropriate for local dev)

**Connect from CLI:**
```bash
# Create topic
docker-compose exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic order.confirmed \
  --partitions 3 --replication-factor 1

# List topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Publish message
docker-compose exec kafka kafka-console-producer --broker-list localhost:9092 --topic order.confirmed

# Consume messages
docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic order.confirmed --from-beginning
```

**Test connection from Spring Boot:**
```properties
spring.cloud.stream.kafka.binder.brokers=localhost:9092
spring.kafka.bootstrap-servers=localhost:9092
```

---

### Zookeeper (Port 2181)
- **Container**: recadero-zookeeper
- **Data Volume**: zookeeper_data, zookeeper_logs (persisted)
- **Role**: Distributed coordination for Kafka cluster

**Verify Zookeeper status:**
```bash
docker-compose exec zookeeper echo ruok | nc localhost 2181
# Expected: imok
```

---

## Key Topics (Auto-created)

Spring Cloud Stream will auto-create these topics on first use:

| Topic | Purpose | Partitions |
|-------|---------|-----------|
| `order.confirmed` | Order payment success | 3 |
| `cart.abandoned` | Cart expiration triggers email | 2 |
| `inventory.updated` | Product stock changes | 1 |
| `user.registered` | New user email verification | 1 |
| `payment.processed` | Payment webhook events | 3 |
| `shipment.updated` | Carrier tracking updates | 2 |

---

## Troubleshooting

### Services won't start
```bash
# Remove containers and restart
docker-compose down -v
docker-compose up -d
```

### PostgreSQL connection refused
```bash
# Check if service is healthy
docker-compose ps postgres

# View logs
docker-compose logs postgres

# Wait a bit longer for startup
sleep 10 && psql -h localhost -U recadero -d recadero
```

### Redis connection refused
```bash
docker-compose ps redis
docker-compose logs redis

# Test with redis-cli
docker-compose exec redis redis-cli PING
```

### Kafka broker not available
```bash
# Verify Zookeeper is running first
docker-compose logs zookeeper

# Wait for Kafka to register with Zookeeper (30-60 seconds)
docker-compose logs kafka | grep "started"

# Check broker status
docker-compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Port conflicts
Edit compose.yaml and change external ports (first port number in `9092:9092`):
```yaml
ports:
  - '9093:9092'  # Use 9093 externally if 9092 is taken
```

---

## Database Schema Structure

After PostgreSQL starts, these schemas are available:

```
recadero (main database)
├── catalog          # Products, categories, inventory
├── users            # User accounts, profiles, addresses
├── cart             # Shopping cart, items, promotions
├── orders           # Orders, payments, shipments
├── promotions       # Discounts, coupons, rules
├── templates        # Page templates, blocks, versions
└── shared           # Audit logs, performance metrics
```

---

## Performance Testing

### Load test PostgreSQL connection pool:
```bash
# Monitor connections
docker-compose exec postgres psql -U recadero -d recadero -c \
  "SELECT count(*) as connections, state FROM pg_stat_activity GROUP BY state;"
```

### Monitor Redis memory usage:
```bash
docker-compose exec redis redis-cli INFO memory
```

### Monitor Kafka lag:
```bash
docker-compose exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group recadero-events \
  --describe
```

---

## Clean Up

### Remove all containers, networks, and volumes:
```bash
docker-compose down -v
```

### Remove only containers (keep volumes):
```bash
docker-compose down
```

---

## Production Considerations

⚠️ **WARNING**: This docker-compose.yaml is for **LOCAL DEVELOPMENT ONLY**.

For production deployment, consider:
- Use managed services (AWS RDS for PostgreSQL, ElastiCache for Redis, MSK for Kafka)
- Enable SSL/TLS for all connections
- Use strong, unique passwords managed via secrets manager
- Enable authentication/authorization
- Set up monitoring and alerting
- Configure backups and disaster recovery
- Use multi-node Kafka cluster with replication factor ≥ 3
- Use PostgreSQL with read replicas
- Implement rate limiting and circuit breakers

See DEPLOYMENT.md for production deployment strategies.

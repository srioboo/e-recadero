# Production Readiness Checklist (T194)

## Graceful shutdown — fixed, was missing

`application-prod.yml` had no shutdown configuration at all, meaning a
`SIGTERM` (e.g. during a rolling deploy or pod eviction) would kill
in-flight requests immediately. Added:

```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

This makes Spring Boot stop accepting new requests but wait up to 30s for
in-flight ones to complete before shutting down. Whatever orchestrator runs
this in production (Kubernetes, ECS, etc.) must be configured with a
`terminationGracePeriodSeconds` (or equivalent) of at least a few seconds
more than this 30s, or the process gets killed before it finishes draining.

## Health check endpoints — already present, confirmed

`application-prod.yml` already exposes `/actuator/health` with
`probes.enabled: true`, which gives Spring Boot's standard
`/actuator/health/liveness` and `/actuator/health/readiness` groups —
exactly what Kubernetes-style liveness/readiness probes expect. Also exposes
`/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`. Nothing to add
here; just wire an orchestrator's probe config to these paths (remember the
`/api/v1` context path: `/api/v1/actuator/health/readiness`, etc.).

## Database backup strategy — not app-configurable; document the requirement

This is an infrastructure/operations decision, not something the
application itself configures. Whichever managed Postgres is used in
production (RDS, Cloud SQL, etc.) should have:
- Automated daily snapshots, 30-day retention (per `tasks.md`'s stated
  target).
- Point-in-time recovery enabled if the provider supports it.
- A documented, periodically-tested restore procedure — an untested backup
  is not a backup.

Not something I can configure from this codebase; flagging as a deployment
prerequisite.

## Logging aggregation — not app-configurable as such; app side already correct

`application-prod.yml` logs to stdout in a structured, timestamped pattern
plus rotating files (`/var/log/recadero/application.log`, 100MB/30 files/1GB
cap) — this is exactly what a log shipper (Fluent Bit, Vector, CloudWatch
Logs agent, etc.) expects to tail. The actual aggregation destination
(ELK, CloudWatch, Datadog, etc.) is an infrastructure choice outside this
repo — flagging as a deployment prerequisite, not a code gap.

## Monitoring alerts — metrics are exposed; alert rules are not app-configurable

`/actuator/prometheus` is already exposed with Micrometer + `observation.metrics`
enabled, so p95 latency (`http_server_requests_seconds` histogram), error
rate (status-code-tagged counters), and DB connection pool usage
(`hikaricp_connections_*`, since HikariCP metrics are auto-registered) are
all already available to scrape. What's missing is the alerting layer itself
(Prometheus Alertmanager rules, Grafana alerts, or a SaaS APM's alert
config) — that's infrastructure setup outside this repo, not something to
hardcode into the application. Flagging as a deployment prerequisite:
someone needs to write the actual alert rules (suggested starting
thresholds, matching `tasks.md`'s targets: p95 > 200ms sustained for GET,
error rate > 1%, connection pool utilization > 80%).

## Rate limiting — known gap, carried over from the security audit

Already flagged in `security-audit.md` (T187): no rate limiting exists at
the application layer. `CouponController`'s validate endpoint and the auth
endpoints are the ones that most need it. Needs a decision before
production: `bucket4j` in-app, or a gateway/WAF in front of the service.

## Summary

| Item | Status |
|---|---|
| Graceful shutdown | **Fixed** — was completely missing |
| Health check endpoints | Already correct, confirmed |
| DB backup strategy | Infra prerequisite, documented, not app code |
| Logging aggregation | App-side output already correct; shipper/destination is an infra prerequisite |
| Monitoring alerts | Metrics already exposed; alert rules are an infra prerequisite |
| Rate limiting | Known gap (carried from T187), not fixed |

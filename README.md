# event-service

# Portal26 - High-Throughput Event Receiver

## Overview

This project implements a high-throughput backend event receiver service that:
- Accepts JSON events via a POST `/ingest` endpoint
- Validates headers (`X-Customer-Tier`)
- Batches and uploads events to S3
- Minimizes S3 writes via size/time thresholds
- Provides observability via logs and metrics

---

## 🔧 Tech Stack

- Java 11
- Spring Boot 3.x
- AWS S3 SDK v2
- Micrometer + Spring Boot Actuator
- Docker

---

## ✅ Features

- POST endpoint: `/ingest`
- Header filtering for `X-Customer-Tier` (`gold`, `silver`, `platinum`)
- Batching rules:
    - Max batch size: 5MB
    - Max delay: 5 seconds
- Metrics exposed at `/actuator/metrics` (e.g., `custom.requests.filtered`, `custom.s3.writes.total`)
- S3 integration using `PutObject`

---

## 🚀 How to Run Locally

### Prerequisites

- Java 11+
- AWS CLI configured locally (`~/.aws/credentials`)
- Docker (for container build/test)


Request:
curl -X POST http://localhost:8080/ingest   -H "Content-Type: application/json"   -H "X-Customer-Tier: gold "   -d '{
"eventTimestamp": "2024-01-11T01:42:50.234200+00:00",
"body": "what is the capital of India?"
}'

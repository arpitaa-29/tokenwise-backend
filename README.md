# TokenWise Backend

**TokenWise Backend** powers real-time wallet intelligence and analytics for Solana tokens.  
It tracks the top token holders ("whales"), ingests their live transaction activity, and exposes actionable analytics via a REST API.

---

## 🚀 Features

- **Top Holder Discovery:** Fetches and caches the top 60 token holders every 5 minutes using Helius RPC and Redis.
- **Real-Time Monitoring:** Registers webhooks with Helius for tracked wallets; receives live buy/sell events.
- **Transaction Analytics:** Filters, parses, and stores relevant transactions in PostgreSQL.
- **RESTful API:** Exposes endpoints for wallet analytics, protocol usage, transaction exports, and more.
- **Time-Based Filtering:** Supports flexible time windows and custom ranges for analytics.
- **CSV/JSON Export:** Download filtered transaction data for further analysis.

---

## 🛠️ Tech Stack

- **Java 17+**
- **Spring Boot**
- **PostgreSQL**
- **Redis**
- **Helius RPC/Webhooks** (for Solana)
- **Scheduler** (`@Scheduled` tasks)
- **OpenAPI/Swagger** (optional)

---

## ⚡ Quick Start

### 1. Clone the Repo

git clone https://github.com/arpitaa-29/tokenwise-dashboard.git
cd tokenwise-dashboard/backend

text

### 2. Configure Environment

- Copy `.env.example` to `.env` (or set environment variables as needed).
- Edit `src/main/resources/application.properties`:
  - Set your PostgreSQL connection
  - Set Redis host/port
  - Add your Helius API key

### 3. Build & Run

./mvnw spring-boot:run

Or use your IDE to run the application
text

The backend runs on [http://localhost:8080](http://localhost:8080) by default.


---


## 🏗️ Architecture Overview

[Scheduled Task (TokenHoldingService)]
↓
Calls Helius → Gets token holders → Stores in Redis every 5 min

[GET /api/token/top-holders]
↓
Uses cached Redis data → returns top 60 wallets

[Helius Webhook Event] ← triggered automatically
↓
POST /api/webhook/transaction
↓
WebhookController → TransactionProcessingService → stores txn in DB

[GET /api/activity/stats]
↓
Reads all stored transactions → calculates stats → sends to frontend


---

## 🛠️ Customization

- **Change the token**: Update the token mint in your scheduler and webhook setup.
- **Add more analytics**: Extend `InsightService` and add new endpoints.
- **Tune performance**: Adjust Redis and PostgreSQL configs as needed

---

## 🙏 Credits

- Built by [Arpita / Your Team]
- Powered by Solana, Spring Boot, PostgreSQL, and Redis

---

**TokenWise Backend** — Real-Time Solana Wallet Analytics  
*Analyze smarter. Trade wiser.*
Copy this into your backend/README.md.
Update any placeholders (team, screenshots, etc.) as needed!

Related
Data Fetching & Caching: Uses Helius RPC to retrieve top 60 token holder addresses every 5 minutes.
Webhook Listeners: Registers webhook endpoints with Helius to receive real-time buy/sell transaction updates.
Data Storage: Stores transaction details in PostgreSQL for analytics and historical analysis.
Task Scheduling: Utilizes a scheduler (e.g., Spring Boot's @Scheduled) for periodic data refreshes.
API Layer: Exposes RESTful endpoints for frontend consumption.

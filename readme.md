# 📈 | Trade Matching Engine

A simplified exchange-style **Trade Matching Engine** implemented in Java.  
The project demonstrates core concepts used in real-world stock exchanges such as
price–time priority, order books, and trade execution.

---

## 🌐 | Overview

This project simulates the core matching logic of a financial exchange.
Orders are matched using **price priority** followed by **time priority**,
with support for partial order execution.

The system is designed to be simple, modular, and extendable.

---

## ✨ | Features

- Limit order support (BUY / SELL)
- Price–time priority matching
- Partial order fills
- In-memory order book
- Trade execution logging
- Clean and extensible architecture

---

## 💻 | Tech Stack

- Java 17
- Maven
- IntelliJ IDEA

---

## 💪 | Project Structure

```text
src/main/java/com/kev/tme
├── engine
│   ├── MatchingEngine.java
│   └── OrderBook.java
│
├── model
│   ├── Order.java
│   ├── OrderSide.java
│   ├── OrderType.java
│   └── Trade.java
│
└── Main.java
```
---

## 💾 | How It Works

1. Orders are submitted to the `MatchingEngine`
2. Orders are stored in an `OrderBook`
    - BUY orders: highest price first
    - SELL orders: lowest price first
3. Orders are matched when buy price is greater than or equal to sell price
4. Trades are generated and logged
5. Partial fills are handled automatically

---

## 🏃‍♂️ | How to Run

### Using IntelliJ IDEA
- Open the project
- Run the `Main.java` file

### Using Maven
```bash
mvn clean compile
mvn exec:java
```

---

## 📄 | Sample Output

```bash
TRADE | BUY=1 SELL=2 PRICE=99.75 QTY=5
TRADE | BUY=1 SELL=3 PRICE=100.50 QTY=5
```

---

## 🚀 | Future Enhancements

- Market orders
- Order cancellation and modification
- Trade history persistence
- Multithreading support
- REST API using Spring Boot



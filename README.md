# Telegram Stripe Payment Bot

A Java-based Telegram bot that generates Stripe payment links based on user input (price, currency, product name, and
quantity).

---

## Getting Started

### 1. Create a Telegram Bot

1. Open Telegram and message [`@BotFather`](https://t.me/BotFather).
2. Run the command `/newbot`.
3. Provide a name and a username (must end in `bot`, e.g., `stripepaybot`).
4. Copy the bot token you receive — **you'll use this in configuration**.

---

### 2. Set Up Stripe Account & Keys

1. Go to [Stripe Dashboard](https://dashboard.stripe.com/apikeys/).
2. Navigate to **Developers > API keys**.
3. Copy the following:
    - **Secret key** → e.g., `sk_test_...`
    - **Publishable key** → e.g., `pk_test_...`
4. Configure redirect URLs:
    - **Success URL** → e.g., `https://yourapp.com/success`
    - **Cancel URL** → e.g., `https://yourapp.com/cancel`

---

### 3. Configuration via Environment Variables

This bot loads configuration in the following priority:

1. **System environment variables** (preferred for production)
2. **Fallback to `config.properties`**

#### Required Variables

| Key                           | Description                       |
|-------------------------------|-----------------------------------|
| `BOT_TOKEN`                   | Telegram bot token                |
| `BOT_USERNAME`                | Telegram bot username             |
| `PAYMENTS_STRIPE_KEY_PUBLIC`  | Stripe public key                 |
| `PAYMENTS_STRIPE_KEY_SECRET`  | Stripe secret key                 |
| `PAYMENTS_STRIPE_SUCCESS_URL` | Redirect after successful payment |
| `PAYMENTS_STRIPE_CANCEL_URL`  | Redirect after canceled payment   |

#### Example `.env` (for local use)

```dotenv
BOT_TOKEN=your-telegram-bot-token
BOT_USERNAME=your-bot-username
PAYMENTS_STRIPE_KEY_PUBLIC=pk_test_...
PAYMENTS_STRIPE_KEY_SECRET=sk_test_...
PAYMENTS_STRIPE_SUCCESS_URL=https://yourapp.com/success
PAYMENTS_STRIPE_CANCEL_URL=https://yourapp.com/cancel
```

---

### 4. Running in Docker

#### Using Dockerfile

You can build and run the application using a standard `Dockerfile`.

##### Build the application

```bash
mvn clean package -DskipTests
```

##### Build the Docker image

```bash
docker build -t my/telegram-bot:latest .
```

##### Run the container

```bash
docker run -d --name telegram-bot --restart=always \
  -e BOT_TOKEN="***" \
  -e BOT_USERNAME="my_bot_name" \
  -e PAYMENTS_STRIPE_KEY_SECRET="sk_live_***" \
  -e PAYMENTS_STRIPE_KEY_PUBLIC="pk_live_***" \
  -e PAYMENTS_STRIPE_SUCCESS_URL="https://yourapp.com/success" \
  -e PAYMENTS_STRIPE_CANCEL_URL="https://yourapp.com/cancel" \
  my/telegram-bot:latest
```

---

## How It Works

1. User starts interaction with `/start`.
2. Bot prompts:
    - Enter price + currency → e.g., `10.00 USD`
    - Enter product name
    - Enter quantity
3. Generates Stripe Checkout link and sends it back.

---

## ⚙️ Project Goals & Platform Constraints

### 🎯 Purpose: Lightweight, Framework-Free Telegram Bot

This project is a **minimalist Java Telegram bot** for creating Stripe payment links, intentionally designed without any
heavyweight frameworks (like Spring Boot or Jakarta EE). Its **small footprint and straightforward architecture** make
it ideal for constrained environments like:

- Raspberry Pi
- ARMv7-based boards
- Lightweight Docker containers

### 🚫 Why No Frameworks?

This bot avoids frameworks for these reasons:

- 🚀 **Fast startup** – no classpath scanning or auto-wiring
- 🧠 **Simple lifecycle** – easy to maintain and debug
- ⚙️ **Minimal dependencies** – only uses Telegram, Stripe, SLF4J/Logback
- 📦 **Tiny Docker image** – suitable for embedded or edge devices
- 🧱 **Custom command flow** – cleanly modeled with interfaces (`CommandHandler`, `StateInputHandler`)

---

## 🏗️ Summary of Design Intent

| Component                              | Purpose                                        |
|----------------------------------------|------------------------------------------------|
| `PaymentBot`                           | Core bot class, Telegram API polling           |
| `SessionManager`                       | In-memory session tracking (price, state, etc) |
| `CommandHandler` / `StateInputHandler` | Handles command flow and data input stages     |
| `MessageSender`                        | Message abstraction (easy for testing)         |
| **Maven Shade Plugin**                 | Creates single runnable JAR                    |
| **ARM32v7-compatible Docker image**    | Can run on devices like Raspberry Pi           |

---

## 🧪 Tech Stack

- Java 17
- TelegramBots API
- Stripe Java SDK
- SLF4J + Logback (logging)
- Maven (build) + Docker (runtime)

---

## 📚 Resources

- [Telegram Bot API Docs](https://core.telegram.org/bots/api)
- [Stripe Java SDK Docs](https://stripe.com/docs/api?lang=java)
- [Stripe Checkout Docs](https://stripe.com/docs/checkout)

---

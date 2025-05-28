# Telegram Stripe Payment Bot

A Java-based Telegram bot that generates Stripe payment links based on user input (price, currency, product name, and
quantity).

---

## Getting Started

### 1.Create a Telegram Bot

1. Open Telegram and message [`@BotFather`](https://t.me/BotFather).
2. Run the command `/newbot`.
3. Provide a name and a username (must end in `bot`, e.g., `stripepaybot`).
4. Copy the bot token you receive — **you'll use this in configuration**.

---

### 2.Set Up Stripe Account & Keys

1. Go to [Stripe Dashboard](https://dashboard.stripe.com/).
2. Navigate to **Developers > API keys**.
3. Copy the following:
    - **Secret key** → e.g., `sk_test_...`
    - (Optional) **Publishable key** → if needed for client-side logic
4. Configure redirect URLs:
    - **Success URL** → e.g., `https://yourapp.com/success`
    - **Cancel URL** → e.g., `https://yourapp.com/cancel`

---

### 3. ⚙️ Configuration via Environment Variables

This bot loads configuration in the following priority:

1. **System environment variables** (preferred for production)
2. **Fallback to `config.properties`**

#### Required Variables

| Key                           | Description                       |
|-------------------------------|-----------------------------------|
| `BOT_TOKEN`                   | Telegram bot token                |
| `BOT_USERNAME`                | Telegram bot username             |
| `PAYMENTS_STRIPE_KEY_SECRET`  | Stripe secret key                 |
| `PAYMENTS_STRIPE_SUCCESS_URL` | Redirect after successful payment |
| `PAYMENTS_STRIPE_CANCEL_URL`  | Redirect after canceled payment   |

#### Example `.env` (for local use)

```dotenv
BOT_TOKEN=your-telegram-bot-token
BOT_USERNAME=your-bot-username
PAYMENTS_STRIPE_KEY_SECRET=sk_test_...
PAYMENTS_STRIPE_SUCCESS_URL=https://yourapp.com/success
PAYMENTS_STRIPE_CANCEL_URL=https://yourapp.com/cancel
```

---

### 4. Running in Docker

#### Dockerfile-Free Build (Jib)

You can build and run using [Jib](https://github.com/GoogleContainerTools/jib) without a Dockerfile.

#### Build Image

```bash
mvn compile jib:dockerBuild -Dimage=kalpak44/telegram-bot:latest
```

#### Run Image

```bash
docker run -e BOT_TOKEN=your-token \
           -e BOT_USERNAME=your-bot \
           -e PAYMENTS_STRIPE_KEY_SECRET=sk_test_... \
           -e PAYMENTS_STRIPE_SUCCESS_URL=https://yourapp.com/success \
           -e PAYMENTS_STRIPE_CANCEL_URL=https://yourapp.com/cancel \
           kalpak44/telegram-bot:latest
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

## Tech Stack

- Java 17
- TelegramBots API
- Stripe Java SDK
- Maven + Jib for containerization
- SLF4J + Logback

---

## Resources

- [Telegram Bot API Docs](https://core.telegram.org/bots/api)
- [Stripe Java SDK Docs](https://stripe.com/docs/api?lang=java)
- [Jib Documentation](https://github.com/GoogleContainerTools/jib)

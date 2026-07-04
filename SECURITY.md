# Security Policy

## Supported Versions

The table below lists the versions of PayStory that are actively supported with security updates:

| Version | Supported          |
| ------- | ------------------ |
| v1.0.x  | :white_check_mark: |
| < v1.0  | :x:                |

## Our Security & Privacy Guarantees

Because PayStory processes personal financial data directly from your device's SMS text messages and notification events, **user privacy is our highest priority**:

1. **Zero Cloud Telemetry of Financial Data**: All transaction notifications, SMS messages, amounts, and merchant data are processed **strictly on-device**. None of this data is ever transmitted to remote logging servers, telemetry systems, or third-party analytic services.
2. **Offline-First Storage**: All transaction data is stored locally in an encrypted/safe SQLite database managed by Android's standard **Room Database** framework.
3. **No Financial Credentials Ever Requested**: PayStory does **not** ask for your banking usernames, passwords, card details, PINs, or open-banking APIs. It reads data purely from passive, user-consented operating system notifications and incoming SMS alerts.

## Reporting a Vulnerability

If you discover a security vulnerability in this project, please **do not open a public GitHub Issue**. Public disclosures expose users to risks unnecessarily. Instead, please report it privately:

1. Send an email with a detailed explanation of the vulnerability and reproduction steps to **security@paystory.org** (or private security contact).
2. We will acknowledge receipt of your vulnerability report within 48 hours.
3. We will work to provide a fix or mitigation plan within 14 business days, and notify you when it has been resolved.
4. A public security advisory (CVE) will be created if appropriate, giving you full credit for the discovery unless you choose to remain anonymous.

## Security Practices in Development

To ensure the safety of this system:
* **Secrets Injections**: Sensitive API keys (like Google Gemini) are never hardcoded in source. They must be configured via `.env` and loaded using the Maps Platform Secrets Gradle Plugin.
* **ProGuard Obfuscation**: Release builds must enable standard ProGuard optimization (`proguard-rules.pro`) to prevent reverse-engineering of transaction parsing regex and database structures.
* **No Broadcast Leaks**: Internal Intents and receivers should restrict visibility by checking actions and ensuring background services only accept validated permissions.

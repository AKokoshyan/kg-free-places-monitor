# KG Free Places Monitor

A lightweight Spring Boot 3 application that monitors free places availability on the [newkg.uslugi.io](https://newkg.uslugi.io) API and sends email notifications when availability changes.

## Features

* Scheduled API polling every 5 minutes.
* Uses `WebClient` with timeout and retry handling.
* Detects changes in API response and sends email notifications.
* Configurable email recipients and scheduler interval.
* Health and info endpoints available via Spring Boot Actuator.

---

## API Behavior

**Endpoint:**

```
POST https://newkg.uslugi.io/lv/api/free-places
```

**Request Body:**

```json
{"reception": "jasla"}
```

**Example Response:**

```json
{
  "free-places": {
    "KLAS_DATE": "05-11-2025",
    "SPR_SWOBODNI_MESTA": null,
    "IS_FINAL": "0"
  }
}
```

If `SPR_SWOBODNI_MESTA` is `null` , there are **no free spaces**. Otherwise, the application sends an email notification.

---

## Project Structure

```
kg-free-places-monitor/
├── pom.xml
├── src/
│   ├── main/java/io/services/monitor/
│   │   ├── MonitorApplication.java
│   │   ├── config/WebClientConfig.java
│   │   ├── dto/KgRequest.java
│   │   ├── dto/KgResponse.java
│   │   ├── service/KgApiClient.java
│   │   ├── service/EmailNotifier.java
│   │   └── service/MonitorService.java
│   └── resources/application.yml
└── README.md
```

---

## Configuration

All application settings are in `src/main/resources/application.yml`.

### Example configuration

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: your_smtp_username
    password: your_smtp_password
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

monitor:
  api:
    url: https://newkg.uslugi.io/lv/api/free-places
    reception: jasla
    timeout-ms: 5000
    retry:
      max-attempts: 3
      backoff-ms: 1000
  schedule:
    cron: "0 */5 * * * *"  # every 5 minutes
  email:
    from: no-reply@your-domain.com
    to: you@your-domain.com, teammate@your-domain.com
    subject: "[KG] Free places status changed"
```

---

## How It Works

1. Every 5 minutes, the application posts `{ "reception": "jasla" }` to the API.
2. If the API response shows available places, an email is sent.
3. You can monitor health via Actuator endpoints:

    * `/actuator/health`
    * `/actuator/info`

---

## Build and Run

### With Maven

```bash
mvn clean package
java -jar target/kg-free-places-monitor-1.0.0.jar
```

### Run in Development Mode

```bash
mvn spring-boot:run
```

---

## Logging

Logs show API responses, polling status, and notifications:

```bash
INFO  i.u.m.service.MonitorService - Polling KG free places...
INFO  i.u.m.service.EmailNotifier  - Notification email sent to [you@your-domain.com]
```

---

## Customization

* **Multiple Receptions:** Extend `MonitorService` to loop over multiple reception values.
* **HTML Emails:** Replace `SimpleMailMessage` with `MimeMessageHelper` for HTML support.

---
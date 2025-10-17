package io.services.monitor.service;
import io.services.monitor.dto.KgResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class EmailNotifier {
  private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

  private final JavaMailSender mailSender;
  private final String from;
  private final String[] recipients;
  private final String subject;

  public EmailNotifier(JavaMailSender mailSender,
      @Value("${monitor.email.from}") String from,
      @Value("${monitor.email.to}") String toCsv,
      @Value("${monitor.email.subject}") String subject) {
    this.mailSender = mailSender;
    this.from = from;
    this.recipients = Arrays.stream(toCsv.split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .toArray(String[]::new);
    this.subject = subject;
  }

  public void notifyChange(KgResponse response) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(from);
    msg.setTo(recipients);
    msg.setSubject(subject);

    KgResponse.FreePlaces fp = response.freePlaces();
    String text = "Detected change in KG free places response:\n" +
        "KLAS_DATE: " + (fp != null ? fp.klasDate() : "<null>") + "\n" +
        "SPR_SWOBODNI_MESTA: " + (fp != null ? fp.freeCount() : "<null>") + "\n" +
        "IS_FINAL: " + (fp != null ? fp.isFinal() : "<null>") + "\n" +
        "\nRaw JSON may contain more fields.";

    msg.setText(text);
    mailSender.send(msg);
    log.info("Notification email sent to {}", Arrays.toString(recipients));
  }
}
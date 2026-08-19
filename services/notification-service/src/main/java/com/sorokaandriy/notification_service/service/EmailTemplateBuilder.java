package com.sorokaandriy.notification_service.service;

import com.sorokaandriy.notification_service.kafka.OrderCreatedEvent;
import com.sorokaandriy.notification_service.kafka.OrderItemEvent;
import com.sorokaandriy.notification_service.kafka.UserRegisteredEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateBuilder {

    private final String shopName;
    private final String frontendUrl;

    public EmailTemplateBuilder(@Value("${app.shop-name}") String shopName,
                                @Value("${app.frontend-url}") String frontendUrl) {
        this.shopName = shopName;
        this.frontendUrl = frontendUrl;
    }

    public String buildRegistrationEmail(UserRegisteredEvent event) {
        return """
                <div style="font-family:Arial,Helvetica,sans-serif;max-width:560px;margin:0 auto;color:#111820">
                  <h2 style="margin-bottom:4px">Вітаємо в %s, %s!</h2>
                  <p>Обліковий запис на <b>%s</b> створено. Тепер ви можете оформлювати замовлення
                     та стежити за їх статусом у особистому кабінеті.</p>
                  <p style="margin:24px 0">
                    <a href="%s" style="background:#111820;color:#fff;padding:12px 20px;
                       text-decoration:none;border-radius:6px">Перейти до магазину</a>
                  </p>
                  <p style="color:#6b7280;font-size:13px">Якщо ви не реєструвалися — просто проігноруйте цей лист.</p>
                </div>
                """.formatted(shopName, event.getFirstName(), event.getEmail(), frontendUrl);
    }

    public String buildOrderEmail(OrderCreatedEvent event) {
        StringBuilder rows = new StringBuilder();
        for (OrderItemEvent item : event.getItems()) {
            rows.append("""
                    <tr>
                      <td style="padding:8px 0;border-bottom:1px solid #e5e7eb">%s</td>
                      <td style="padding:8px 0;border-bottom:1px solid #e5e7eb;text-align:center">%d шт.</td>
                      <td style="padding:8px 0;border-bottom:1px solid #e5e7eb;text-align:right">%s грн</td>
                    </tr>
                    """.formatted(item.getProductName(), item.getQuantity(), item.getPrice()));
        }

        return """
                <div style="font-family:Arial,Helvetica,sans-serif;max-width:560px;margin:0 auto;color:#111820">
                  <h2 style="margin-bottom:4px">Замовлення №%d прийнято</h2>
                  <p>Дякуємо за покупку, %s. Ми повідомимо вас, щойно замовлення буде передане в доставку.</p>
                  <table style="width:100%%;border-collapse:collapse;margin:16px 0">%s</table>
                  <p style="font-size:18px"><b>Разом: %s грн</b></p>
                  <p style="color:#6b7280;font-size:13px">Адреса доставки: %s</p>
                  <p style="margin:24px 0">
                    <a href="%s/orders" style="background:#111820;color:#fff;padding:12px 20px;
                       text-decoration:none;border-radius:6px">Мої замовлення</a>
                  </p>
                </div>
                """.formatted(event.getOrderId(), event.getCustomerName(), rows, event.getTotalPrice(),
                event.getDeliveryAddress(), frontendUrl);
    }
}

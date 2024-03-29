package com.bristle.orderservice.converter;

import com.bristle.orderservice.model.OrderEntity;
import com.bristle.orderservice.model.ProductEntryEntity;
import com.bristle.proto.order.Order;
import com.bristle.proto.order.ProductEntry;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderEntityConverter {
    private final ProductEntryEntityConverter m_productEntryConverter;

    OrderEntityConverter(ProductEntryEntityConverter productEntryConverter) {
        m_productEntryConverter = productEntryConverter;
    }

    public OrderEntity protoToEntity(Order orderProto) {
        int orderId = orderProto.getOrderId();
        String customerOrderId = orderProto.getCustomerOrderId();
        String customerId = orderProto.getCustomerId();
        long dueDate = orderProto.getDueDate();
        String note = orderProto.getNote();
        long deliveredAt = orderProto.getDeliveredAt();
        long issuedAt = orderProto.getIssuedAt();

        OrderEntity result = new OrderEntity(
                orderId == Integer.MIN_VALUE ? null : orderId,
                customerOrderId.equals("") ? null : customerOrderId,
                customerId.equals("") ? null : customerId,
                dueDate == Long.MIN_VALUE ? null : Instant.ofEpochMilli(dueDate * 1000).atZone(ZoneId.of("UTC")).toLocalDate(),
                note.equals("") ? null : note,
                deliveredAt == Long.MIN_VALUE ? null : LocalDateTime.ofEpochSecond(deliveredAt, 0, ZoneOffset.UTC),
                issuedAt == Long.MIN_VALUE ? null : LocalDateTime.ofEpochSecond(issuedAt, 0, ZoneOffset.UTC)
                , null);

        if (orderProto.getProductEntryCount() > 0) {
            List<ProductEntryEntity> entityList
                    = orderProto.getProductEntryList().stream().map(proto -> m_productEntryConverter.protoToEntity(proto, result)).collect(Collectors.toList());
            result.setProductEntries(entityList);
        } else {
            result.setProductEntries(Collections.emptyList());
        }

        return result;
    }

    public Order entityToProto(OrderEntity orderEntity) {
        Integer orderId = orderEntity.getOrderId();
        String customerOrderId = orderEntity.getCustomerOrderId();
        String customerId = orderEntity.getCustomerId();
        LocalDate dueDate = orderEntity.getDueDate();
        String note = orderEntity.getNote();
        LocalDateTime deliveredAt = orderEntity.getDeliveredAt();
        LocalDateTime issuedAt = orderEntity.getIssuedAt();

        Order.Builder result = Order.newBuilder()
                .setOrderId(orderId == null ? Integer.MIN_VALUE : orderId)
                .setCustomerOrderId(customerOrderId == null ? "" : customerOrderId)
                .setCustomerId(customerId == null ? "" : customerId)
                .setDueDate(dueDate == null ? Long.MIN_VALUE : dueDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC))
                .setNote(note == null ? "" : note)
                .setDeliveredAt(deliveredAt == null ? Long.MIN_VALUE : deliveredAt.toEpochSecond(ZoneOffset.UTC))
                .setIssuedAt(issuedAt == null ? Long.MIN_VALUE : issuedAt.toEpochSecond(ZoneOffset.UTC));

        if (orderEntity.getProductEntries().size() > 0) {
            List<ProductEntry> productEntriesList = orderEntity.getProductEntries().stream()
                    .map( item -> {
                        return m_productEntryConverter.entityToProto(item, orderEntity);
                    }).collect(Collectors.toList());
            result.addAllProductEntry(productEntriesList);
        }

        return result.build();
    }
}

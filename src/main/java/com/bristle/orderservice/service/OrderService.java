package com.bristle.orderservice.service;


import com.bristle.orderservice.converter.OrderEntityConverter;
import com.bristle.orderservice.model.OrderEntity;
import com.bristle.orderservice.repository.OrderEntitySpec;
import com.bristle.orderservice.repository.OrderRepository;
import com.bristle.orderservice.repository.ProductEntryRepository;
import com.bristle.proto.order.Order;
import com.bristle.proto.order.OrderFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository m_orderRepository;

    private final ProductEntryRepository m_productEntryRepository;

    private final OrderEntityConverter m_orderConverter;

    Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    public OrderService(OrderRepository m_orderRepository,
                        OrderEntityConverter orderConverter,
                        ProductEntryRepository productEntryRepository) {
        this.m_orderRepository = m_orderRepository;
        this.m_orderConverter = orderConverter;
        this.m_productEntryRepository = productEntryRepository;
    }

    @Transactional
    public Order upsertOrder(Order orderProto) throws Exception {
        OrderEntity orderEntity = m_orderConverter.protoToEntity(orderProto);
        if (orderEntity.getOrderId() != null) {
            // this means we're updating
            m_productEntryRepository.deleteProductEntryEntitiesByOrderIdFk(orderEntity.getOrderId());
        }
        m_orderRepository.save(orderEntity);
        // here the order id actually gets assigned by hibernate
        OrderEntity upsertedOrder = m_orderRepository.findOrderEntityByOrderId(orderEntity.getOrderId());
        return m_orderConverter.entityToProto(upsertedOrder);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrders(OrderFilter filter, Integer pageIndex, Integer pageSize) throws Exception {
        Specification<OrderEntity> spec = new Specification<OrderEntity>() {
            @Override
            public Predicate toPredicate(Root<OrderEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return null;
            }
        };
        if (filter.getOrderId() != Integer.MIN_VALUE) {
            spec = spec.and(OrderEntitySpec.equalOrderId(filter.getOrderId()));
        }

        if (!filter.getCustomerOrderId().equals("")) {
            spec = spec.and(OrderEntitySpec.equalCustomerOrderId(filter.getCustomerOrderId()));
        }

        if (!filter.getCustomerId().equals("")) {
            spec = spec.and(OrderEntitySpec.likeCustomerId(filter.getCustomerId()));
        }

        if (filter.getDueDateFrom() != 0 && filter.getDueDateFrom() != Long.MIN_VALUE && filter.getDueDateTo() != Long.MIN_VALUE) {
            spec = spec.and(OrderEntitySpec.dueDateBetween(new Date(filter.getDueDateFrom()),
                    new Date(filter.getDueDateTo())));
        }

        if (filter.getIssuedAtFrom() != 0 && filter.getIssuedAtFrom() != Long.MIN_VALUE && filter.getIssuedAtTo() != Long.MIN_VALUE) {
            spec = spec.and(OrderEntitySpec.issuedBetween(
                    LocalDateTime.ofEpochSecond(
                            filter.getIssuedAtFrom(), 0, ZoneOffset.UTC),
                    LocalDateTime.ofEpochSecond(
                            filter.getIssuedAtTo(), 0, ZoneOffset.UTC)));
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "issuedAt");
        Pageable paging = PageRequest.of(pageIndex, pageSize, sort);

        List<OrderEntity> rs = m_orderRepository.findAll(Specification.where(spec), paging).toList();
        return rs.stream().map(m_orderConverter::entityToProto).collect(Collectors.toList());
    }

    @Transactional
    public Order deleteOrder(Integer orderId) {
        OrderEntity toBeDeleted = m_orderRepository.findOrderEntityByOrderId(orderId);
        m_orderRepository.delete(toBeDeleted);
        return m_orderConverter.entityToProto(toBeDeleted);
    }

}

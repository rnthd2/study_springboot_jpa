package jpabook.jpashop.repository.order.simplequery;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public OrderSimpleQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name; //LAZY 초기화(영속성 컨텍스트를 찾아 가져오고 없으면 db쿼리 호출) // 회원이 한 명이 여러개 주문을 조회 했을 땐 영속성 컨텍스트에 있음으로 db 쿼리 조회가 필요없음
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;//LAZY 초기화(영속성 컨텍스트를 찾아 가져오고 없으면 db쿼리 호출)
    }
}
package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.OrderService;
import jpabook.jpashop.service.query.OrderDto;
import jpabook.jpashop.service.query.OrderQueryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;


/**
 * order simple api controller 와는 달리
 * x to many 인 경우, fetch join이 약간 더 복잡하다
 * db입장에서 many to x 보다 더 많은 부하가 있을 수 있다 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final OrderQueryService orderQueryService;

    /**
     * 엔티티를 조회해서 그대로 반환 V1
     * @return
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        return orderQueryService.ordersV1(new OrderSearch());
    }

    // JPA1 BOOK으로 모든 주문서가 있다면 한번만 db조회하고 영속성 컨텍스트로 가져오면 되는데 아니면 조회 쿼리가 너무 많다...
    /**
     * 엔티티 조회 후 DTO로 변환 V2
     * @return
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        return orderQueryService.ordersV2(new OrderSearch());
    }

    // 여기서 join fetch 를 하는 순간 주문이 2개에 한 주문 당 아이템이 2개씩이니 4 row가 출력된다, 1:n인 경우 n만큼 출력된다 --> distinct!
    // *************** 중요 ************ //
    //페이징 안됨 ......!       //
    //firstResult/maxResults specified with collection fetch; applying in memory!
    //order item 기준으로 페이징 되기 때문에 일대다 패치조인에선 메모리에서 페이징을 해버린다(매우 위험하다...)페이징이 안되니 패치조인을 하면 안된다....
    //일대다 관계로 패치조인하고 일(order)기준으로 페이징이 되었으면 좋겠는데 다(order item) 기준으로 페이징이 되어버리니 문제다...
    //
    //일대다의 다를 패치 조인하면 안된다....
    //1:n:m을 패치 조인하면 정확성이 떨어진다
    //오로지 1:n의 관계에서만 하자
    //
    // *************** 중요 ************ //
    /**
     * 패치 조인으로 쿼리 수 최적화 V3
     * @return
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        return orderQueryService.ordersV3();
    }


    /**
     * 컬렉션 페이징과 한계 돌파 V3.1
     * @return
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit)
    {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        for (Order order : orders) {
            System.out.println("order = " + order + " id = " + order.getId());
        }
        List<OrderDto> collect = orders.stream().map(o -> new OrderDto(o)).collect(Collectors.toList());
        return collect;
    }

    /**
     * JPA에서 DTO를 직접 조회 V4
     * @return
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * 컬렉션 조회 최적화 - 일대다 관계인 컬렉션은 IN 절을 활용해서 메모리에 미리 조회해서 최적화 V5
     * @return
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5(){
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * 플랫 데이터 최적화 - JOIN 결과를 그대로 조회 후 애플리케이션에서 원하는 모양으로 직접 변환 V6
     * @return
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6(){
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(Collectors.toList());
    }


}



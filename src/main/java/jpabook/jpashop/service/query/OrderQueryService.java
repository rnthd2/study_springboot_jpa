package jpabook.jpashop.service.query;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Open session in view
 */
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;

    public List<Order> ordersV1(OrderSearch orderSearch){
        List<Order> all = orderRepository.findAllByString(orderSearch);
        for (Order order : all) {
            order.getMember();
            order.getDelivery().getAddress();

            // orderItems과 orderItems 안의 정보를 강제 초기화 해줌
            // hibernate5module로 기본 설정이 되어있고, LAZY설정이 되어있으면 뿌리질않아 강제 초기화로 조회하여 뿌릴 수 있도록 설정한다
            // 양방향은 무조건 한쪽은 json ignore로 설정해야 한다
            // 다른 예제들처럼 api entity를 직접 노출하지 말아야한다.... 이 예시를 사용하면 안된다
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    public List<OrderDto> ordersV2(OrderSearch orderSearch){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream().map(o -> new OrderDto(o)).collect(Collectors.toList());
        return collect;
    }

    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        for (Order order : orders) {
            System.out.println("order = " + order + " id = " + order.getId());
        }
        List<OrderDto> collect = orders.stream().map(o -> new OrderDto(o)).collect(Collectors.toList());
        return collect;
    }
}

/*
Open session in view
database transaction을 시작할 때, jpa의 영속성 컨텍스트가 database 커넥션을 가져온다(service에서 @Transaction으로 시작할 때)

1. Spring.jpa open session in view:true(기본값)인 경우,
api의 경우, user에게 반환 할때까지,
view의 경우, 렌더링해서 그릴 때까지 커넥션을 살려둔다
….영속성 컨텍스트와 디비 커넥션은 끝까지 살려둔다
-> 지연로딩이 가능한 이유!

그렇지만! 단점은….
너무 오랜시간 디비 커넥션 리소스를 사용하기 때문에 커넥션이 말라버림…!!!

2. Spring.jpa open session in view:false 경우,
트랜잭션을 종료할 때 영속성 컨텍스트를 닫고, 디비 커넥션도 반환한다. 따라서 커넥션 리소스를 낭비하지 않는다..
Db 커넥션을 짧게 유지한다…!
사용자 요청이 많은 경우, 유연하게 쓸 수 있
음
-> 지연로딩을 쓸 수 없음…! Service, repository에서만 지연로딩을 쓸 수 있음
￼
아래 컨트롤러를 호출하였을때, OSIV가 off되어있어 영속성 컨텍스트가 컨트롤러에서 유지 될 수 없어 에러가 발생한다
-> 필요한 소스를 트랜잭션 안으로 넣는다..! -> 새로 Service class를 만들고 transaction true로 설정하고 거기서 비즈니스 로직을 넣는다…!
 */

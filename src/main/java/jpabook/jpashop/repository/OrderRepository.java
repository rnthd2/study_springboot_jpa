package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sun.org.apache.xpath.internal.operations.Bool;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.QItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QOrder.order;
import static jpabook.jpashop.domain.QOrderItem.orderItem;
import static jpabook.jpashop.domain.item.QItem.item;

@Repository
public class OrderRepository {
    private final EntityManager em;

    private final JPAQueryFactory query;

    public OrderRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }


    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
//주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
//회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class) .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
    return query.getResultList();
    }

    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
//주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
//회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);


        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }

    public List<Order> findAll(OrderSearch orderSearch){
//        QOrder order = QOrder.order; >> order를 QOrder.order로 하고 static Import 하면 초기화도 줄일 수 있다

        List<Order> orders = query
                .select(order)
                .from(order)
                .join(order.member, member).fetchJoin()
                .join(order.orderItems, orderItem).fetchJoin()
                .join(orderItem.item, item).fetchJoin()
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName())) // where and condition
                .limit(1000)
                .fetch();

        return orders;
    }

    private BooleanExpression nameLike(String memberName) {
        if(!StringUtils.hasText(memberName))
            return null;
        return member.name.like(memberName);
    }

    private BooleanExpression statusEq(OrderStatus statusCond){
        if(statusCond == null)
            return null;
        return order.status.eq(statusCond);

    }

    //fetch join
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o from Order o"
                + " join fetch o.member m "
                + " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o"
                        + " join fetch o.member m "
                        + " join fetch o.delivery d", Order.class
        ).getResultList();
    }


    //jpql 의 distinct 는? db의 distinct는 모든 데이터가 같았을 때 중복제거 하지만 jpa에서는 자체적으로 order(엔티티)가 같은 값이면 하나의 데이터는 버림
    public List<Order> findAllWithItem() {
        return em.createQuery("select distinct o from Order o " +
                " join fetch o.member m" +
                " join fetch o.delivery d" +
                " join fetch o.orderItems oi" +
                " join fetch oi.item i", Order.class)
                .setFirstResult(1)
                .setMaxResults(100)
                .getResultList();
    }

//    public List<Order> findAll(OrderSearch orderSearch){}
    /*distinct 전 --> distinct 후는 이터 하나 삭제 --> 이슈 없음
    [
    {
        "orderId": 4,
            "orderName": "userA",
            "orderDate": "2020-11-20T19:01:42.310768",
            "orderStatus": "ORDER",
            "address": {
        "city": "seoul",
                "street": "1",
                "zipcode": "1111"
    },
        "orderItems": [
        {
            "itemName": "JPA1 BOOK",
                "orderPrice": 10000,
                "count": 1
        },
        {
            "itemName": "JPA2 BOOK",
                "orderPrice": 20000,
                "count": 2
        }
        ]
    },
    {
        "orderId": 4,
            "orderName": "userA",
            "orderDate": "2020-11-20T19:01:42.310768",
            "orderStatus": "ORDER",
            "address": {
        "city": "seoul",
                "street": "1",
                "zipcode": "1111"
    },
        "orderItems": [
        {
            "itemName": "JPA1 BOOK",
                "orderPrice": 10000,
                "count": 1
        },
        {
            "itemName": "JPA2 BOOK",
                "orderPrice": 20000,
                "count": 2
        }
        ]
    } ... */
}

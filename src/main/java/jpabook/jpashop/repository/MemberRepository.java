package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    @PersistenceContext
    private final EntityManager em;

//    @PersistenceUnit
//    private EntityManagerFactory emf;

//    public MemberRepository(EntityManager em) {
//        this.em = em;
//    }

    public void save(Member member){
        em.persist(member); //todo id값을 pk로 저장하기 때문에 항상 save를 한다는 보장된다..?
    }

    public Member findOne(Long id){
        return em.find(Member.class, id);
    }

    public List<Member> findAll(){
        //sql은 table을 대상으로 쿼리하지만 아래 코드는 엔티티를 대상으로 쿼리한다
        //jpql 문법을사용하였다
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findName(String name){
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }


}

package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * findBy*** 의 룰에 따라
     * select m from Member m where m.name = ? 로 조회
     * 강력하다...!
     */
    List<Member> findByName(String name);
}

package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
//@Rollback(value = false)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager em;


    @Test
//    @Rollback(value = false)
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long saveId = memberService.join(member);

        //then
//        em.flush(); //db에 실제 반영
        assertEquals(member, memberRepository.findById(saveId));
    }

    @Test
//            (expected = IllegalStateException.class) junit4
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");
        
        //when
        memberService.join(member1);

        //junit5
//        Exception exception = assertThrows(IllegalStateException.class, () -> memberService.join(member2));
//        assertEquals(IllegalStateException.class, exception.getMessage());
        try {
            memberService.join(member2);
//            assertThrows(IllegalStateException.class, () -> memberService.join(member2));
        } catch (IllegalStateException ie){
            return;
        }

        //then
//        fail("예외가 발생해야 됩니다.");
    }
        

}
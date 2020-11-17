package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) //jpa 조회 최적화, 읽기 전용에서만 readOnly true option
@RequiredArgsConstructor
public class MemberService {

//    @Autowired
//field injection
    private final MemberRepository memberRepository;

//    setter injection
//    @Autowired
//    public void setMemberRepository(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    /**
     * 회원 가입
     *
     * @param member
     * @return
     */
    @Transactional //default readOnly false라 읽기 전용이 아닌경우 이렇게
    public Long join(Member member){
        validateDuplicateMember(member);    //중복 회원 검증 로직
        memberRepository.save(member);
        return member.getId();
    }
    private void validateDuplicateMember(Member member){
        //아래 비즈니스 로직을 실무에서 돌릴때
        //멀티쓰레드인 경우 db충돌이 일어날 수 있기 때문에 member name 을 unique 할 필요가 있다
        List<Member> findMembers = memberRepository.findName(member.getName());

        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 전체 조회
     *
     * @return
     */
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    /**
     * 회원 단건 조회
     *
     * @param id
     * @return
     */
    public Member findOne(Long id){
        return memberRepository.findOne(id);
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}

package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /**
     * memberService     @Transaction:OFF
     * memberRepository  @Transaction:ON
     * logRepository     @Transaction:ON
     */
    @Test
    void outerTxOff_success(){
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);

        //then : 모든 데이터가 정상 저장 된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService     @Transaction:OFF
     * memberRepository  @Transaction:ON
     * logRepository     @Transaction:ON Exception
     */
    @Test
    void outerTxOff_fail(){
        //given
        String username = "로그예외_outerTxOff_fail";

        //when
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        //then : 로그데이터만 롤백 된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService     @Transaction:ON
     * memberRepository  @Transaction:OFF
     * logRepository     @Transaction:OFF
     */
    @Test
    void singleTx(){
        //given
        String username = "singleTx";

        //when
        memberService.joinV1(username);

        //then : 모든 데이터가 정상 저장 된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService     @Transaction:ON
     * memberRepository  @Transaction:ON
     * logRepository     @Transaction:ON
     */
    @Test
    void outerTxOn_success(){
        //given
        String username = "outerTxOn_success";

        //when
        memberService.joinV1(username);

        //then : 모든 데이터가 정상 저장 된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService     @Transaction:ON
     * memberRepository  @Transaction:ON
     * logRepository     @Transaction:ON Exception
     */
    @Test
    void outerTxOn_fail(){
        //given
        String username = "로그예외_outerTxOn_fail";

        //when
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        //then : 모든 데이터가 롤백 된다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService     @Transaction:ON
     * memberRepository  @Transaction:ON
     * logRepository     @Transaction:ON Exception
     */
    @Test
    void recoverException_fail(){
        //given
        String username = "로그예외_recoverException_fail";

        //when
        assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        //then : 모든 데이터가 롤백 된다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService     @Transaction:ON
     * memberRepository  @Transaction:ON
     * logRepository     @Transaction:ON(REQUIRES_NEW) Exception
     */
    @Test
    void recoverException_success(){
        //given
        String username = "로그예외_recoverException_success";

        //when
        memberService.joinV2(username);

        //then : member 저장, log 롤백
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }
}
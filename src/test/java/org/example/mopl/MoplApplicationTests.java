package org.example.mopl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// 빌드 실패해서 수정하였다. User 도메인 연동 후 제거 할 예정.
@SpringBootTest
@ActiveProfiles("test")
class MoplApplicationTests {

    @Test
    void contextLoads() {
    }

}

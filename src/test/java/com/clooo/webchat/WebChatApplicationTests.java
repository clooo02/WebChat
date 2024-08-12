package com.clooo.webchat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
class WebChatApplicationTests {

    @Test
    void contextLoads() {
        Integer v = 264;
        Byte[] bytes = {v.byteValue()};
        System.out.println(Arrays.toString(bytes));
    }

}

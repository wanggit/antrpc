package antrpc.test;

import antrpc.test.api.UserDTO;
import antrpc.test.web.UserResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AntrpcTestApplication.class})
public class AntrpcTestApplicationTest {

    @Autowired private UserResource userResource;

    @Test
    public void testSendMessage() {
        UserDTO userDTO = userResource.findById();
        System.out.println(userDTO);
    }

    @Test
    public void testSendBigOneMessage() {
        UserDTO userDTO = userResource.findBigOne();
        System.out.println(userDTO);
    }

    @Test
    public void testManySendBigOneMessage() {
        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            System.out.println(userResource.findBigOne());
            System.out.println(System.currentTimeMillis() - start);
        }
    }

    @Test
    public void testManySendMessage() {
        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            System.out.println(userResource.findById());
            System.out.println(System.currentTimeMillis() - start);
        }
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme

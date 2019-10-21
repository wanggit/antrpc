package antrpc.test.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/http-user")
public class HttpUserEndpoint {

    @Autowired private UserResource userResource;

    @GetMapping("/more-logs")
    public void createMoreRunLogs() {
        for (int i = 0; i < 100000; i++) {
            userResource.findById();
        }
    }
}

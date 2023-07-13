package com.nt.backend.workflow.controller;

import com.nt.backend.workflow.dto.UserIdDTO;
import com.nt.backend.workflow.service.HttpService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/demo/")
@Slf4j
@AllArgsConstructor
public class DemoController {

    private final HttpService httpService;

    @GetMapping("callback")
    public void callback(){
        log.info("触发器测试ok");
    }

    @PostMapping("demo")
    public void demo(@RequestBody UserIdDTO dto) throws IOException {
        String leaderIdByStartUserId = httpService.findLeaderIdByStartUserId(dto.getUserId());
        log.info(leaderIdByStartUserId);
    }
}

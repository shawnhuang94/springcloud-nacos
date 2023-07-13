package com.nt.backend.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author LoveMyOrange
 * @create 2022-10-15 19:47
 */
@Component
@Slf4j
public class TimerListener implements ExecutionListener {
    @Resource
    private RepositoryService repositoryService;
    @Override
    public void notify(DelegateExecution execution) {
        log.info("========");
        System.err.println("exection触发了");
    }
}

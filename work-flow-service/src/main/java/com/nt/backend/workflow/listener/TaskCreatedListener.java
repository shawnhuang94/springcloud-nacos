package com.nt.backend.workflow.listener;

import com.nt.backend.workflow.utils.SpringContextHolder;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author LoveMyOrange
 * @create 2022-10-15 14:51
 */
@Component
public class TaskCreatedListener implements TaskListener {
    @Resource
    private TaskService taskService;
    @Override
    public void notify(DelegateTask delegateTask) {
            if("100000".equals(delegateTask.getAssignee())){
                Object autoRefuse = delegateTask.getVariable("autoRefuse");
                if(autoRefuse==null){
                    taskService.addComment(delegateTask.getId(),delegateTask.getProcessInstanceId(),"opinion","审批人为空,自动通过");
                    taskService.complete(delegateTask.getId());
                }
                else{
                    taskService.addComment(delegateTask.getId(),delegateTask.getProcessInstanceId(),"opinion","审批人为空,自动驳回");
                    RuntimeService runtimeService = SpringContextHolder.getBean(RuntimeService.class);
                    runtimeService.deleteProcessInstance(delegateTask.getProcessInstanceId(),"审批人为空,自动驳回");
                }
            }
    }
}

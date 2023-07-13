package com.nt.backend.workflow.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.nt.backend.workflow.dto.json.ChildNode;
import com.nt.backend.workflow.dto.json.Properties;
import com.nt.backend.workflow.dto.json.UserInfo;
import com.nt.backend.workflow.enums.AssigneeTypeEnums;
import com.nt.backend.workflow.exception.WorkFlowException;
import com.nt.backend.workflow.service.HttpService;
import com.nt.backend.workflow.utils.BpmnModelUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.nt.backend.workflow.common.CommonConstants.START_USER_INFO;


/**
 * @author LoveMyOrange
 * @create 2022-10-15 13:35
 */
@Component
public class CounterSignListener implements ExecutionListener {
    @Resource
    private RepositoryService repositoryService;

    @Resource
    private HistoryService historyService;

    @Resource
    private HttpService httpService;

    @Override
    public void notify(DelegateExecution execution) {
        String currentActivityId = execution.getCurrentActivityId();
        Process mainProcess = repositoryService.getBpmnModel(execution.getProcessDefinitionId()).getMainProcess();
        //查询当前流程实例,获取对应的流程发起人
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
        UserTask userTask = (UserTask) mainProcess.getFlowElement(currentActivityId);
        String dingDing = mainProcess.getAttributeValue("http://flowable.org/bpmn", "DingDing");
        JSONObject jsonObject = JSONObject.parseObject(dingDing, new TypeReference<JSONObject>() {
        });
        String processJson = jsonObject.getString("processJson");
        ChildNode childNode = JSONObject.parseObject(processJson, new TypeReference<ChildNode>(){});
        List<String> assigneeList= new ArrayList<>();
        String variable=currentActivityId+"assigneeList";
        List usersValue = (List) execution.getVariable(variable);
        if(usersValue==null){
            ChildNode currentNode = BpmnModelUtils.getChildNode(childNode, currentActivityId);
            if(currentNode==null){
                throw new WorkFlowException("查找审批人失败,请联系管理员重试");
            }
            Properties props = currentNode.getProps();
            String assignedType = props.getAssignedType();
            Map<String, Object> nobody = props.getNobody();
            if(AssigneeTypeEnums.ASSIGN_USER.getTypeName().equals(assignedType)){
                List<UserInfo> assignedUser = props.getAssignedUser();
                for (UserInfo userInfo : assignedUser) {
                    assigneeList.add(userInfo.getId());
                }
            }
            else if(AssigneeTypeEnums.SELF_SELECT.getTypeName().equals(assignedType)){

            }
            else if(AssigneeTypeEnums.LEADER_TOP.getTypeName().equals(assignedType)){
                throw new WorkFlowException("暂不做这个功能,等发版!");
            }
            else if(AssigneeTypeEnums.LEADER.getTypeName().equals(assignedType)){
                try {
                    String leaderIdByStartUserId = httpService.findLeaderIdByStartUserId(historicProcessInstance.getStartUserId());
                    if (StringUtils.isNotEmpty(leaderIdByStartUserId)){
                        assigneeList.add(leaderIdByStartUserId);
                    }else {
                        assigneeList.add("381496");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                /*
                这里代码应该怎么写???   你想想
                应该是 通过leader 的code  查出来leader里面的人  然后添加到
                assigneeList.addAll()  既可
                不过本项目没有添加leader的CRUD页面 所以 先这样,
                怎么实现就是上述实现手段
                下面角色也一样, 希望我们<布尔什维克>的
                同志们
                可以举一反三 一通百通
                 */
//                throw new WorkFlowException("当前只是简单的系统 没有RBAC功能,各位可以自己实现!");
            }
            else if(AssigneeTypeEnums.ROLE.getTypeName().equals(assignedType)){

//                throw new WorkFlowException("当前只是简单的系统 没有RBAC功能,各位可以自己实现!");
            }
            else if(AssigneeTypeEnums.SELF.getTypeName().equals(assignedType)){
                String startUserJson = execution.getVariable(START_USER_INFO, String.class);
                UserInfo userInfo = JSONObject.parseObject(startUserJson, new TypeReference<UserInfo>() {
                });
                assigneeList.add(userInfo.getId());
            }
            else if(AssigneeTypeEnums.FORM_USER.getTypeName().equals(assignedType)){
                String formUser = props.getFormUser();
                List<JSONObject> assigneeUsers = execution.getVariable(formUser, List.class);
                if(assigneeUsers!=null){
                    for (JSONObject assigneeUser : assigneeUsers) {
                        assigneeList.add(assigneeUser.getString("id"));
                    }
                }

            }

            if(CollUtil.isEmpty(assigneeList)){
                String handler = MapUtil.getStr(nobody, "handler");
                if("TO_PASS".equals(handler)){
                    assigneeList.add("100000");
                    execution.setVariable(variable,assigneeList);
                }
                else if("TO_REFUSE".equals(handler)){
                    execution.setVariable("autoRefuse",Boolean.TRUE);
                    assigneeList.add("100000");
                    execution.setVariable(variable,assigneeList);
                }
                else if("TO_ADMIN".equals(handler)){
                    assigneeList.add("381496");
                    execution.setVariable(variable,assigneeList);
                }
                else if("TO_USER".equals(handler)){
                    Object assignedUserObj = nobody.get("assignedUser");
                    if(assignedUserObj!=null ){
                        List<JSONObject> assignedUser =(List<JSONObject>)assignedUserObj;
                        if(assignedUser.size()>0){
                            for (JSONObject object : assignedUser) {
                                assigneeList.add(object.getString("id"));
                            }
                            execution.setVariable(variable,assigneeList);
                        }
                        else{
                            assigneeList.add("100000");
                            execution.setVariable(variable,assigneeList);
                        }

                    }

                }
                else{
                    throw new WorkFlowException("找不到审批人,请检查配置!!!");
                }
            }
            else{
                execution.setVariable(variable,assigneeList);
            }
        }
        else{
        }
    }
}

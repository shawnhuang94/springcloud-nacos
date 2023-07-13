package com.nt.backend.workflow.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nt.backend.workflow.common.WorkFlowConstants;
import com.nt.backend.workflow.dto.json.*;
import com.nt.backend.workflow.dto.json.Properties;
import com.nt.backend.workflow.enums.ModeEnums;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static org.flowable.bpmn.model.ImplementationType.IMPLEMENTATION_TYPE_CLASS;
import static org.flowable.bpmn.model.ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION;

/**
 * @author LoveMyOrange
 * @create 2022-10-10 17:47
 */
public class BpmnModelUtils {

    private static String id(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    private static ServiceTask serviceTask(String name) {
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setName(name);
        return serviceTask;
    }

    public static SequenceFlow connect(String from, String to, List<SequenceFlow> sequenceFlows, Map<String, ChildNode> childNodeMap, Process process) {
        SequenceFlow flow = new SequenceFlow();
        String  sequenceFlowId = id("sequenceFlow");
        if(process.getFlowElement(from) !=null && process.getFlowElement(from) instanceof ExclusiveGateway){
            ChildNode childNode = childNodeMap.get(to);
            if(childNode!=null){
                String parentId = childNode.getParentId();
                if(StringUtils.isNotBlank(parentId)){
                    ChildNode parentNode = childNodeMap.get(parentId);
                    if(parentNode!=null){
                        if(Type.CONDITION.type.equals(parentNode.getType()) ){
                            sequenceFlowId=parentNode.getId();
                            flow.setName(parentNode.getName());

                            if(Boolean.FALSE.equals(parentNode.getTypeElse())){
                                //解析条件表达式
                                Properties props = parentNode.getProps();
                                String expression = props.getExpression();
                                List<GroupsInfo> groups = props.getGroups();
                                String groupsType = props.getGroupsType();
                                if(StringUtils.isNotBlank(expression)){
                                    flow.setConditionExpression("${"+expression+"}");
                                }
                                else {

                                    StringBuffer conditionExpression=new StringBuffer();
                                    conditionExpression.append("${ ");

                                    for (int i = 0; i < groups.size(); i++) {
                                        conditionExpression.append(" ( ");
                                        GroupsInfo group = groups.get(i);
                                        List<String> cids = group.getCids();
                                        String groupType = group.getGroupType();
                                        List<ConditionInfo> conditions = group.getConditions();
                                        for (int j = 0; j < conditions.size(); j++) {
                                            conditionExpression.append(" ");
                                            ConditionInfo condition = conditions.get(j);
                                            String compare = condition.getCompare();
                                            String id = condition.getId();
                                            String title = condition.getTitle();
                                            List<Object> value = condition.getValue();
                                            String valueType = condition.getValueType();
                                            if("String".equals(valueType)){
                                                if("=".equals(compare)){
                                                    String str = StringUtils.join(value, ",");
                                                    str="'"+str+"'";
                                                    conditionExpression.append(" "+ WorkFlowConstants.EXPRESSION_CLASS+"strEqualsMethod("+id+","+str+") " );
                                                }
                                                else{
                                                    List<String> tempList=new ArrayList<>();
                                                    for (Object o : value) {
                                                        String s = o.toString();
                                                        s="'"+s+"'";
                                                        tempList.add(s);
                                                    }
                                                    String str = StringUtils.join(tempList, ",");
//                                                String str = StringUtils.join(value, ",");
                                                    conditionExpression.append(" "+ WorkFlowConstants.EXPRESSION_CLASS+"strContainsMethod("+id+","+str+") " );
                                                }
                                            }
                                            else if("Number".equals(valueType)){
                                                String str = StringUtils.join(value, ",");
                                                if("=".equals(compare)){
                                                    conditionExpression.append(" "+ WorkFlowConstants.EXPRESSION_CLASS+"numberEquals("+id+","+str+") " );
                                                }
                                                else if(">".equals(compare)){
                                                    conditionExpression.append(" "+ WorkFlowConstants.EXPRESSION_CLASS+"numberGt("+id+","+str+") " );
                                                }
                                                else if(">=".equals(compare)){
                                                    conditionExpression.append(" "+ WorkFlowConstants.EXPRESSION_CLASS+"numberGtEquals("+id+","+str+") " );
                                                }
                                                else if("<".equals(compare)){
                                                    conditionExpression.append(" "+ WorkFlowConstants.EXPRESSION_CLASS+"numberLt("+id+","+str+") " );
                                                }
                                                else if("<=".equals(compare)){
                                                    conditionExpression.append(" "+ WorkFlowConstants.EXPRESSION_CLASS+"numberLtEquals("+id+","+str+") " );
                                                }
                                                else if("IN".equals(compare)){
                                                    conditionExpression.append(" "+ WorkFlowConstants.EXPRESSION_CLASS+"numberContains("+id+","+str+") " );
                                                }
                                                else if("B".equals(compare)){
                                                    conditionExpression.append("  "+ WorkFlowConstants.EXPRESSION_CLASS+"b("+id+","+str+") " );
                                                }
                                                else if("AB".equals(compare)){
                                                    conditionExpression.append("  "+ WorkFlowConstants.EXPRESSION_CLASS+"ab("+id+","+str+") " );
                                                }
                                                else if("BA".equals(compare)){
                                                    conditionExpression.append("  "+ WorkFlowConstants.EXPRESSION_CLASS+"ba("+id+","+str+") " );
                                                }
                                                else if("ABA".equals(compare)){
                                                    conditionExpression.append("  "+ WorkFlowConstants.EXPRESSION_CLASS+"aba("+id+","+str+") " );
                                                }
                                            }
                                            else if("User".equals(valueType)){
                                                List<String> userIds=new ArrayList<>();
                                                for (Object o : value) {
                                                    JSONObject obj=(JSONObject)o;
                                                    userIds.add(obj.getString("id"));
                                                }
                                                String str = StringUtils.join(userIds, ",");
                                                conditionExpression.append(" "+ WorkFlowConstants.EXPRESSION_CLASS+"userStrContainsMethod("+id+","+str+") " );
                                            }
                                            else if("Dept".equals(valueType)){
                                                List<String> userIds=new ArrayList<>();
                                                for (Object o : value) {
                                                    JSONObject obj=(JSONObject)o;
                                                    userIds.add(obj.getString("id"));
                                                }
                                                String str = StringUtils.join(userIds, ",");
                                                conditionExpression.append(" "+ WorkFlowConstants.EXPRESSION_CLASS+"deptStrContainsMethod("+id+","+str+") " );
                                            }
                                            else{
                                                continue;
                                            }

                                            if(conditions.size()>1 && j!=(conditions.size()-1)){
                                                if("OR".equals(groupType)){
                                                    conditionExpression.append(" || ");
                                                }
                                                else {
                                                    conditionExpression.append(" && ");
                                                }
                                            }

                                            if(i==(conditions.size()-1)){
                                                conditionExpression.append(" ");
                                            }
                                        }


                                        conditionExpression.append(" ) ");

                                        if(groups.size()>1 && i!=(groups.size()-1) ){
                                            if("OR".equals(groupsType)){
                                                conditionExpression.append(" || ");
                                            }
                                            else {
                                                conditionExpression.append(" && ");
                                            }
                                        }


                                    }
                                    conditionExpression.append("} ");
                                    flow.setConditionExpression(conditionExpression.toString());
                                }
                            }
                        }
                    }
                }
            }
        }
        flow.setId(sequenceFlowId);
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        sequenceFlows.add(flow);
        return flow;
    }

    private static String stringEquals(ConditionInfo condition) {
        return null;
    }


    public static StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId(WorkFlowConstants.START_EVENT_ID);
        startEvent.setInitiator("applyUserId");
        return startEvent;
    }

    public static EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId(WorkFlowConstants.END_EVENT_ID);
        return endEvent;
    }


    public static String create(String froworkflow, ChildNode flowNode, Process process, BpmnModel bpmnModel, List<SequenceFlow> sequenceFlows, Map<String,ChildNode> childNodeMap) throws InvocationTargetException, IllegalAccessException {
        String nodeType = flowNode.getType();
        if (Type.CONCURRENTS.isEqual(nodeType)) {
            return createParallelGatewayBuilder(froworkflow, flowNode,process,bpmnModel,sequenceFlows,childNodeMap);
        } else if (Type.CONDITIONS.isEqual(nodeType)) {
            return createExclusiveGatewayBuilder(froworkflow, flowNode,process,bpmnModel,sequenceFlows,childNodeMap);
        } else if (Type.USER_TASK.isEqual(nodeType)) {
            childNodeMap.put(flowNode.getId(),flowNode);
            JSONObject incoming = flowNode.getIncoming();
            incoming.put("incoming", Collections.singletonList(froworkflow));
            String id = createTask(process,flowNode,sequenceFlows,childNodeMap);
            // 如果当前任务还有后续任务，则遍历创建后续任务
            ChildNode children = flowNode.getChildren();
            if (Objects.nonNull(children) &&StringUtils.isNotBlank(children.getId())) {
                return create(id, children,process,bpmnModel,sequenceFlows,childNodeMap);
            } else {
                return id;
            }
        }
        else if(Type.ROOT.isEqual(nodeType)){
            childNodeMap.put(flowNode.getId(),flowNode);
            JSONObject incoming = flowNode.getIncoming();
            incoming.put("incoming", Collections.singletonList(froworkflow));
            String id = createTask(process,flowNode,sequenceFlows,childNodeMap);
            // 如果当前任务还有后续任务，则遍历创建后续任务
            ChildNode children = flowNode.getChildren();
            if (Objects.nonNull(children) &&StringUtils.isNotBlank(children.getId())) {
                return create(id, children,process,bpmnModel,sequenceFlows,childNodeMap);
            } else {
                return id;
            }
        }
        else if(Type.DELAY.isEqual(nodeType)){
            childNodeMap.put(flowNode.getId(),flowNode);
            JSONObject incoming = flowNode.getIncoming();
            incoming.put("incoming", Collections.singletonList(froworkflow));
            String id = createDelayTask(process,flowNode,sequenceFlows,childNodeMap);
            // 如果当前任务还有后续任务，则遍历创建后续任务
            ChildNode children = flowNode.getChildren();
            if (Objects.nonNull(children) &&StringUtils.isNotBlank(children.getId())) {
                return create(id, children,process,bpmnModel,sequenceFlows,childNodeMap);
            } else {
                return id;
            }
        }
        else if(Type.TRIGGER.isEqual(nodeType)){
            childNodeMap.put(flowNode.getId(),flowNode);
            JSONObject incoming = flowNode.getIncoming();
            incoming.put("incoming", Collections.singletonList(froworkflow));
            String id = createTriggerTask(process,flowNode,sequenceFlows,childNodeMap);
            // 如果当前任务还有后续任务，则遍历创建后续任务
            ChildNode children = flowNode.getChildren();
            if (Objects.nonNull(children) &&StringUtils.isNotBlank(children.getId())) {
                return create(id, children,process,bpmnModel,sequenceFlows,childNodeMap);
            } else {
                return id;
            }
        }
        else if(Type.CC.isEqual(nodeType)){
            childNodeMap.put(flowNode.getId(),flowNode);
            JSONObject incoming = flowNode.getIncoming();
            incoming.put("incoming", Collections.singletonList(froworkflow));
            String id = createServiceTask(process,flowNode,sequenceFlows,childNodeMap);
            // 如果当前任务还有后续任务，则遍历创建后续任务
            ChildNode children = flowNode.getChildren();
            if (Objects.nonNull(children) &&StringUtils.isNotBlank(children.getId())) {
                return create(id, children,process,bpmnModel,sequenceFlows,childNodeMap);
            } else {
                return id;
            }
        }
        else {
            throw new RuntimeException("未知节点类型: nodeType=" + nodeType);
        }
    }

    private static String createExclusiveGatewayBuilder(String forworkflow,  ChildNode flowNode,Process process,BpmnModel bpmnModel,List<SequenceFlow> sequenceFlows,Map<String,ChildNode> childNodeMap) throws InvocationTargetException, IllegalAccessException {
        childNodeMap.put(flowNode.getId(),flowNode);
        String name =flowNode.getName();
        String exclusiveGatewayId = flowNode.getId();
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(exclusiveGatewayId);
        exclusiveGateway.setName(name);
        process.addFlowElement(exclusiveGateway);
        process.addFlowElement(connect(forworkflow, exclusiveGatewayId,sequenceFlows,childNodeMap,process));

        if (Objects.isNull(flowNode.getBranchs()) && Objects.isNull(flowNode.getChildren())) {
            return exclusiveGatewayId;
        }
        List<ChildNode> flowNodes = flowNode.getBranchs();
        List<String> incoming = Lists.newArrayListWithCapacity(flowNodes.size());
        List<JSONObject> conditions = Lists.newCopyOnWriteArrayList();
        for (ChildNode element : flowNodes) {
            Boolean typeElse = element.getTypeElse();
            if(Boolean.TRUE.equals(typeElse)){
                exclusiveGateway.setDefaultFlow(element.getId());
            }
            childNodeMap.put(element.getId(),element);
            ChildNode childNode = element.getChildren();

            String nodeName = element.getName();
            Properties props = element.getProps();
            String expression = props.getExpression();


            if (Objects.isNull(childNode) ||  StringUtils.isBlank(childNode.getId())) {

                incoming.add(exclusiveGatewayId);
                JSONObject condition = new JSONObject();
                condition.fluentPut("nodeName", nodeName)
                        .fluentPut("expression", expression)
                        .fluentPut("groups",props.getGroups())
                        .fluentPut("groupsType",props.getGroupsType()
                                )
                        .fluentPut("elseSequenceFlowId",element.getId());
                conditions.add(condition);
                continue;
            }
            // 只生成一个任务，同时设置当前任务的条件
            JSONObject incomingObj = childNode.getIncoming();
            incomingObj.put("incoming", Collections.singletonList(exclusiveGatewayId));
            String identifier = create(exclusiveGatewayId, childNode,process,bpmnModel,sequenceFlows,childNodeMap);
            List<SequenceFlow> flows = sequenceFlows.stream().filter(flow -> StringUtils.equals(exclusiveGatewayId, flow.getSourceRef()))
                    .collect(Collectors.toList());
            flows.stream().forEach(
                    e -> {
                        if (StringUtils.isBlank(e.getName()) && StringUtils.isNotBlank(nodeName)) {
                            e.setName(nodeName);
                        }
                        // 设置条件表达式
                        if (Objects.isNull(e.getConditionExpression()) && StringUtils.isNotBlank(expression)) {
                            e.setConditionExpression(expression);
                        }
                    }
            );
            if (Objects.nonNull(identifier)) {
                incoming.add(identifier);
            }
        }


        ChildNode childNode = flowNode.getChildren();

        if (Objects.nonNull(childNode) &&StringUtils.isNotBlank(childNode.getId()) ) {
            String parentId = childNode.getParentId();
            ChildNode parentChildNode = childNodeMap.get(parentId);
            boolean conFlag = Type.CONCURRENTS.type
                .equals(parentChildNode.getType());
            if(!conFlag) {
                String type = childNode.getType();
                if(!Type.EMPTY.type.equals(type)){
                }
                else{
                    if(Type.CONDITIONS.type.equals(parentChildNode.getType())){
                      String endExId=  parentChildNode.getId()+"end";
                      process.addFlowElement(createExclusiveGateWayEnd(endExId));
                        if (incoming == null || incoming.isEmpty()) {
                            return create(exclusiveGatewayId, childNode, process, bpmnModel, sequenceFlows,
                                childNodeMap);
                        }
                        else {
                            JSONObject incomingObj = childNode.getIncoming();
                            // 所有 service task 连接 end exclusive gateway
                            incomingObj.put("incoming", incoming);
                            FlowElement flowElement = bpmnModel.getFlowElement(incoming.get(0));
                            // 1.0 先进行边连接, 暂存 nextNode
                            ChildNode nextNode = childNode.getChildren();
                            childNode.setChildren(null);
                            String identifier = endExId;
                            for (int i = 0; i < incoming.size(); i++) {
                                process.addFlowElement(connect(incoming.get(i), identifier, sequenceFlows,childNodeMap,process));
                            }

                            //  针对 gateway 空任务分支 添加条件表达式
                            if (!conditions.isEmpty()) {
                                FlowElement flowElement1 = bpmnModel.getFlowElement(identifier);
                                // 获取从 gateway 到目标节点 未设置条件表达式的节点
                                List<SequenceFlow> flows = sequenceFlows.stream().filter(
                                    flow -> StringUtils.equals(flowElement1.getId(), flow.getTargetRef()))
                                    .filter(
                                        flow -> StringUtils.equals(flow.getSourceRef(), exclusiveGatewayId))
                                    .collect(Collectors.toList());
                                flows.stream().forEach(sequenceFlow -> {
                                    if (!conditions.isEmpty()) {
                                        JSONObject condition = conditions.get(0);
                                        String nodeName = condition.getString("nodeName");
                                        String expression = condition.getString("expression");

                                        if (StringUtils.isBlank(sequenceFlow.getName()) && StringUtils
                                            .isNotBlank(nodeName)) {
                                            sequenceFlow.setName(nodeName);
                                        }
                                        // 设置条件表达式
                                        if (Objects.isNull(sequenceFlow.getConditionExpression())
                                            && StringUtils.isNotBlank(expression)) {
                                            sequenceFlow.setConditionExpression(expression);
                                        }

                                        FlowElement flowElement2 = process.getFlowElement(sequenceFlow.getId());
                                        if(flowElement2!=null){
                                            flowElement2.setId(condition.getString("elseSequenceFlowId"));
                                            exclusiveGateway.setDefaultFlow(flowElement2.getId());;
                                        }

                                        conditions.remove(0);
                                    }
                                });

                            }

                            // 1.1 边连接完成后，在进行 nextNode 创建
                            if (Objects.nonNull(nextNode) &&StringUtils.isNotBlank(nextNode.getId())) {
                                return create(identifier, nextNode, process, bpmnModel, sequenceFlows,
                                    childNodeMap);
                            } else {
                                return identifier;
                            }
                        }


                    }
                }
            }
            else{
                System.err.println("-");
            }
        }
        return exclusiveGatewayId;
    }

    public static ExclusiveGateway createExclusiveGateWayEnd(String id){
        ExclusiveGateway exclusiveGateway=new ExclusiveGateway();
        exclusiveGateway.setId(id);
        return exclusiveGateway;
    }

    private static ParallelGateway createParallelGateWayEnd(String id){
        ParallelGateway parallelGateway=new ParallelGateway();
        parallelGateway.setId(id);
        return parallelGateway;
    }

    private static String createParallelGatewayBuilder(String forworkflow, ChildNode flowNode,Process process,BpmnModel bpmnModel,List<SequenceFlow> sequenceFlows,Map<String,ChildNode> childNodeMap) throws InvocationTargetException, IllegalAccessException {
        childNodeMap.put(flowNode.getId(),flowNode);
        String name = flowNode.getName();
        ParallelGateway parallelGateway = new ParallelGateway();
        String parallelGatewayId = flowNode.getId();
        parallelGateway.setId(parallelGatewayId);
        parallelGateway.setName(name);
        process.addFlowElement(parallelGateway);
        process.addFlowElement(connect(forworkflow, parallelGatewayId,sequenceFlows,childNodeMap,process));

        if (Objects.isNull(flowNode.getBranchs()) && Objects.isNull(flowNode.getChildren())) {
            return parallelGatewayId;
        }

        List<ChildNode> flowNodes = flowNode.getBranchs();
        List<String> incoming = Lists.newArrayListWithCapacity(flowNodes.size());
        for (ChildNode element : flowNodes) {
            childNodeMap.put(element.getId(),element);
            ChildNode childNode = element.getChildren();
            if (Objects.isNull(childNode) ||  StringUtils.isBlank(childNode.getId())) {
                incoming.add(parallelGatewayId);
                continue;
            }
            String identifier = create(parallelGatewayId, childNode,process,bpmnModel,sequenceFlows,childNodeMap);
            if (Objects.nonNull(identifier)) {
                incoming.add(identifier);
            }
        }

        ChildNode childNode = flowNode.getChildren();
        if (Objects.nonNull(childNode) &&StringUtils.isNotBlank(childNode.getId())) {
            String parentId = childNode.getParentId();
            ChildNode parentChildNode = childNodeMap.get(parentId);
            boolean conFlag = Type.CONCURRENTS.type
                .equals(parentChildNode.getType());
            if(!conFlag){
                String type = childNode.getType();
                if(!Type.EMPTY.type.equals(type)){

                }
                else{
                    if(Type.CONCURRENTS.type.equals(parentChildNode.getType())){
                        String endExId=  parentChildNode.getId()+"end";
                        process.addFlowElement(createParallelGateWayEnd(endExId));
                        // 普通结束网关
                        if (CollectionUtils.isEmpty(incoming)) {
                            return create(parallelGatewayId, childNode,process,bpmnModel,sequenceFlows,childNodeMap);
                        }
                        else {
                            JSONObject incomingObj = childNode.getIncoming();
                            // 所有 service task 连接 end parallel gateway
                            incomingObj.put("incoming", incoming);
                            FlowElement flowElement = bpmnModel.getFlowElement(incoming.get(0));
                            // 1.0 先进行边连接, 暂存 nextNode
                            ChildNode nextNode = childNode.getChildren();
                            childNode.setChildren(null);
                            String identifier = endExId;
                            for (int i = 0; i < incoming.size(); i++) {
                                FlowElement flowElement1 = bpmnModel.getFlowElement(incoming.get(i));
                                process.addFlowElement(connect(flowElement1.getId(), identifier,sequenceFlows,childNodeMap,process));
                            }
                            // 1.1 边连接完成后，在进行 nextNode 创建
                            if (Objects.nonNull(nextNode)&&StringUtils.isNotBlank(nextNode.getId())) {
                                return create(identifier, nextNode,process,bpmnModel,sequenceFlows,childNodeMap);
                            } else {
                                return identifier;
                            }
                        }
                    }
                }
            }
            else{
                String type = childNode.getType();
                if(!Type.EMPTY.type.equals(type)){

                }
                else{
                    if(Type.CONCURRENTS.type.equals(parentChildNode.getType())){
                        String endExId=  parentChildNode.getId()+"end";
                        process.addFlowElement(createParallelGateWayEnd(endExId));
                        // 普通结束网关
                        if (CollectionUtils.isEmpty(incoming)) {
                            return create(parallelGatewayId, childNode,process,bpmnModel,sequenceFlows,childNodeMap);
                        }
                        else {
                            JSONObject incomingObj = childNode.getIncoming();
                            // 所有 service task 连接 end parallel gateway
                            incomingObj.put("incoming", incoming);
                            FlowElement flowElement = bpmnModel.getFlowElement(incoming.get(0));
                            // 1.0 先进行边连接, 暂存 nextNode
                            ChildNode nextNode = childNode.getChildren();
                            childNode.setChildren(null);
                            String identifier = endExId;
                            for (int i = 0; i < incoming.size(); i++) {
                                FlowElement flowElement1 = bpmnModel.getFlowElement(incoming.get(i));
                                process.addFlowElement(connect(flowElement1.getId(), identifier,sequenceFlows,childNodeMap,process));
                            }
                            // 1.1 边连接完成后，在进行 nextNode 创建
                            if (Objects.nonNull(nextNode) &&StringUtils.isNotBlank(nextNode.getId())) {
                                return create(identifier, nextNode,process,bpmnModel,sequenceFlows,childNodeMap);
                            } else {
                                return identifier;
                            }
                        }
                    }
                }
            }

        }
        return parallelGatewayId;
    }

    private static String createTask(Process process,ChildNode flowNode,List<SequenceFlow> sequenceFlows,Map<String,ChildNode> childNodeMap) {
        JSONObject incomingJson = flowNode.getIncoming();
        List<String> incoming = incomingJson.getJSONArray("incoming").toJavaList(String.class);
        // 自动生成id
//        String id = id("serviceTask");
        String id=flowNode.getId();
        if (incoming != null && !incoming.isEmpty()) {
            UserTask userTask = new UserTask();
            userTask.setName(flowNode.getName());
            userTask.setId(id);
            process.addFlowElement(userTask);
            process.addFlowElement(connect(incoming.get(0), id,sequenceFlows,childNodeMap,process));

            ArrayList<FlowableListener> taskListeners = new ArrayList<>();
            FlowableListener taskListener = new FlowableListener();
            // 事件类型,
            taskListener.setEvent(TaskListener.EVENTNAME_CREATE);
            // 监听器类型
            taskListener.setImplementationType(IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
            // 设置实现了，这里设置监听器的类型是delegateExpression，这样可以在实现类注入Spring bean.
            taskListener.setImplementation("${taskCreatedListener}");
            taskListeners.add(taskListener);
            userTask.setTaskListeners(taskListeners);
            if("root".equalsIgnoreCase(id)){
            }
            else{
                ArrayList<FlowableListener> listeners = new ArrayList<>();
                FlowableListener activitiListener = new FlowableListener();
                // 事件类型,
                activitiListener.setEvent(ExecutionListener.EVENTNAME_START);
                // 监听器类型
                activitiListener.setImplementationType(IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
                // 设置实现了，这里设置监听器的类型是delegateExpression，这样可以在实现类注入Spring bean.
                activitiListener.setImplementation("${counterSignListener}");
                listeners.add(activitiListener);
                userTask.setExecutionListeners(listeners);
                Properties props = flowNode.getProps();
                String mode = props.getMode();
                MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();
                // 审批人集合参数
                multiInstanceLoopCharacteristics.setInputDataItem(userTask.getId()+"assigneeList");
                // 迭代集合
                multiInstanceLoopCharacteristics.setElementVariable("assigneeName");
                // 并行
                multiInstanceLoopCharacteristics.setSequential(false);
                userTask.setAssignee("${assigneeName}");
                // 设置多实例属性
                userTask.setLoopCharacteristics(multiInstanceLoopCharacteristics);
                if(ModeEnums.OR.getTypeName().equals(mode)){
                    multiInstanceLoopCharacteristics.setCompletionCondition("${nrOfCompletedInstances/nrOfInstances > 0}");
                }
                else if (ModeEnums.NEXT.getTypeName().equals(mode)){
                    multiInstanceLoopCharacteristics.setSequential(true);
                }

                JSONObject timeLimit = props.getTimeLimit();
                if(timeLimit!=null && !timeLimit.isEmpty()){
                    JSONObject timeout = timeLimit.getJSONObject("timeout");
                    if(timeout!=null && !timeout.isEmpty()){
                        String unit = timeout.getString("unit");
                        Integer value = timeout.getInteger("value");
                        if(value>0){
                            List<BoundaryEvent> boundaryEvents= new ArrayList<>();
                            BoundaryEvent boundaryEvent= new BoundaryEvent();
                            boundaryEvent.setId(id("boundaryEvent"));
                            boundaryEvent.setAttachedToRefId(id);
                            boundaryEvent.setAttachedToRef(userTask);
                            boundaryEvent.setCancelActivity(Boolean.TRUE);
                            TimerEventDefinition timerEventDefinition = new TimerEventDefinition();
                            timerEventDefinition.setTimeDuration("PT"+value+unit);
                            timerEventDefinition.setId(id("timerEventDefinition"));
                            boundaryEvent.addEventDefinition(timerEventDefinition);
                            FlowableListener flowableListener = new FlowableListener();
                            flowableListener.setEvent(ExecutionListener.EVENTNAME_END);
                            flowableListener.setImplementationType(IMPLEMENTATION_TYPE_CLASS);
                            flowableListener.setImplementation("com.dingding.workflow.listener.TimerListener");
                            List<FlowableListener> listenerList= new ArrayList<>();
                            listenerList.add(flowableListener);
                            boundaryEvent.setExecutionListeners(listenerList);
                            process.addFlowElement(boundaryEvent);
                            boundaryEvents.add(boundaryEvent);
                            userTask.setBoundaryEvents(boundaryEvents);
                        }
                    }
                }

            }
        }
        return id;
    }

    private static String createDelayTask(Process process,ChildNode flowNode,List<SequenceFlow> sequenceFlows,Map<String,ChildNode> childNodeMap) {
        JSONObject incomingJson = flowNode.getIncoming();
        List<String> incoming = incomingJson.getJSONArray("incoming").toJavaList(String.class);
        // 自动生成id
//        String id = id("serviceTask");
        String id=flowNode.getId();
        if (incoming != null && !incoming.isEmpty()) {
            Properties props = flowNode.getProps();
            String type = props.getType();
            IntermediateCatchEvent intermediateCatchEvent = new IntermediateCatchEvent();
            intermediateCatchEvent.setName(flowNode.getName());
            intermediateCatchEvent.setId(id);
            process.addFlowElement(intermediateCatchEvent);
            process.addFlowElement(connect(incoming.get(0), id,sequenceFlows,childNodeMap,process));
            TimerEventDefinition timerEventDefinition = new TimerEventDefinition();
            if("FIXED".equals(type)){
                Long time = props.getTime();
                String unit = props.getUnit();

                timerEventDefinition.setTimeDuration("PT"+time+unit);
                timerEventDefinition.setId(id("timerEventDefinition"));
                intermediateCatchEvent.addEventDefinition(timerEventDefinition);
            }
            else{
                String dateTime = props.getDateTime();
                Date date= new Date();
                String format = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
                String[] split = format.split("-");
                dateTime=split[0]+"-"+split[1]+"-"+split[2]+" "+dateTime;
                timerEventDefinition.setTimeDate(dateTime);
                intermediateCatchEvent.addEventDefinition(timerEventDefinition);
            }
        }
        return id;
    }

    private static String createTriggerTask(Process process,ChildNode flowNode,List<SequenceFlow> sequenceFlows,Map<String,ChildNode> childNodeMap) {
        JSONObject incomingJson = flowNode.getIncoming();
        List<String> incoming = incomingJson.getJSONArray("incoming").toJavaList(String.class);
        // 自动生成id
//        String id = id("serviceTask");
        String id=flowNode.getId();
        if (incoming != null && !incoming.isEmpty()) {
            Properties props = flowNode.getProps();
            String type = props.getType();
            if("WEBHOOK".equals(type)){
                HttpInfo http = props.getHttp();
                HttpServiceTask serviceTask= new HttpServiceTask();
                serviceTask.setType("http");
                List<FieldExtension> fieldExtensions= new ArrayList<>();
                FieldExtension requestMethod= new FieldExtension();
                requestMethod.setFieldName("requestMethod");
                requestMethod.setStringValue(http.getMethod());
                fieldExtensions.add(requestMethod);

                FieldExtension requestUrl= new FieldExtension();
                requestUrl.setFieldName("requestUrl");
                requestUrl.setStringValue(http.getUrl());
                fieldExtensions.add(requestUrl);

                List<Map<String, Object>> headers = http.getHeaders();
                Map<String,Object> header= new HashMap<>();
                header.put("isField",false);
                header.put("name","Content-Type");
                header.put("value","application/json");
                headers.add(header);

                FieldExtension requestHeaders= new FieldExtension();
                requestHeaders.setFieldName("requestHeaders");
                String s = JSONObject.toJSONString(headers);
                try {
                    s = Base64.encode(s.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                requestHeaders.setExpression("${"+ WorkFlowConstants.EXPRESSION_CLASS+ "requestHeaders(execution,"+"'"+s+"'"+")"+"}");
                fieldExtensions.add(requestHeaders);

                List<Map<String, Object>> params = http.getParams();
                FieldExtension requestBody= new FieldExtension();
                requestBody.setFieldName("requestBody");
                String bodyStr = JSONObject.toJSONString(params);
                try {
                    bodyStr = Base64.encode(bodyStr.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                requestBody.setExpression("${"+ WorkFlowConstants.EXPRESSION_CLASS+ "requestBody(execution,"+"'"+bodyStr+"'"+")"+"}");
                fieldExtensions.add(requestBody);

                FieldExtension requestCharset= new FieldExtension();
                requestCharset.setFieldName("requestBodyEncoding");
                requestCharset.setStringValue("UTF-8");
                fieldExtensions.add(requestCharset);
                serviceTask.setFieldExtensions(fieldExtensions);

                serviceTask.setName(flowNode.getName());
                serviceTask.setId(id);

                process.addFlowElement(serviceTask);
                process.addFlowElement(connect(incoming.get(0), id,sequenceFlows,childNodeMap,process));
            }
            else{
                EmailInfo email = props.getEmail();
                ServiceTask serviceTask=new ServiceTask();
                serviceTask.setType("mail");
                List<FieldExtension> fieldExtensions= new ArrayList<>();

                FieldExtension emailFrom= new FieldExtension();
                emailFrom.setFieldName("from");
                emailFrom.setStringValue("2471089198@qq.com");
                fieldExtensions.add(emailFrom);

                FieldExtension emailTo= new FieldExtension();
                emailTo.setFieldName("to");
                emailTo.setStringValue(StrUtil.join(",",email.getTo()));
                fieldExtensions.add(emailTo);

                FieldExtension emailSubject= new FieldExtension();
                emailSubject.setFieldName("subject");
                emailSubject.setStringValue(email.getSubject());
                fieldExtensions.add(emailSubject);

                FieldExtension emailContent= new FieldExtension();
                emailContent.setFieldName("text");
                String content = email.getContent();
                try {
                    content = Base64.encode(content.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                emailContent.setExpression("${"+ WorkFlowConstants.EXPRESSION_CLASS+ "mailContent(execution,"+"'"+content+"'"+")"+"}");
                fieldExtensions.add(emailContent);
                serviceTask.setName(flowNode.getName());
                serviceTask.setId(id);
                serviceTask.setFieldExtensions(fieldExtensions);
                process.addFlowElement(serviceTask);
                process.addFlowElement(connect(incoming.get(0), id,sequenceFlows,childNodeMap,process));
            }

        }
        return id;
    }

    private static String createHttpTask(Process process,ChildNode flowNode,List<SequenceFlow> sequenceFlows,Map<String,ChildNode> childNodeMap) {
        JSONObject incomingJson = flowNode.getIncoming();
        List<String> incoming = incomingJson.getJSONArray("incoming").toJavaList(String.class);
        // 自动生成id
//        String id = id("serviceTask");
        String id=flowNode.getId();
        if (incoming != null && !incoming.isEmpty()) {
            Properties props = flowNode.getProps();
            String type = props.getType();
            IntermediateCatchEvent intermediateCatchEvent = new IntermediateCatchEvent();
            intermediateCatchEvent.setName(flowNode.getName());
            intermediateCatchEvent.setId(id);
            process.addFlowElement(intermediateCatchEvent);
            process.addFlowElement(connect(incoming.get(0), id,sequenceFlows,childNodeMap,process));
            TimerEventDefinition timerEventDefinition = new TimerEventDefinition();
            if("FIXED".equals(type)){
                Long time = props.getTime();
                String unit = props.getUnit();

                timerEventDefinition.setTimeDuration("PT"+time+unit);
                timerEventDefinition.setId(id("timerEventDefinition"));
                intermediateCatchEvent.addEventDefinition(timerEventDefinition);
            }
            else{
                String dateTime = props.getDateTime();
                Date date= new Date();
                String format = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
                String[] split = format.split("-");
                dateTime=split[0]+"-"+split[1]+"-"+split[2]+" "+dateTime;
                timerEventDefinition.setTimeDate(dateTime);
                intermediateCatchEvent.addEventDefinition(timerEventDefinition);
            }
        }
        return id;
    }


    private static String createServiceTask(Process process,ChildNode flowNode,List<SequenceFlow> sequenceFlows,Map<String,ChildNode> childNodeMap) {
        JSONObject incomingJson = flowNode.getIncoming();
        List<String> incoming = incomingJson.getJSONArray("incoming").toJavaList(String.class);
        String id=flowNode.getId();
        if (incoming != null && !incoming.isEmpty()) {
            Properties props = flowNode.getProps();
            String type = props.getType();
            ServiceTask serviceTask = new ServiceTask();
            serviceTask.setName(flowNode.getName());
            serviceTask.setId(id);
            process.addFlowElement(serviceTask);
            process.addFlowElement(connect(incoming.get(0), id,sequenceFlows,childNodeMap,process));
            List<UserInfo> assignedUser = props.getAssignedUser();
            List<FieldExtension> fieldExtensionList = new ArrayList<>();
            FieldExtension fieldExtension = new FieldExtension();
            fieldExtension.setFieldName("ccUser");
            fieldExtension.setStringValue(JSONObject.toJSONString(props.getAssignedUser()));
            fieldExtensionList.add(fieldExtension);
            List<FlowableListener> flowableListeners = new ArrayList<>();
            serviceTask.setImplementation("com.dingding.workflow.listener.CcListener");
            serviceTask.setImplementationType("class");
            serviceTask.setFieldExtensions(fieldExtensionList);
        }
        return id;
    }

    private enum Type {

        /**
         * 并行事件
         */
        CONCURRENTS("CONCURRENTS", ParallelGateway.class),
        CONCURRENT("CONCURRENT", SequenceFlow.class),
        /**
         * 排他事件
         */
        CONDITION("CONDITION", ExclusiveGateway.class),
        CONDITIONS("CONDITIONS", ExclusiveGateway.class),
        /**
         * 任务
         */
        USER_TASK("APPROVAL", UserTask.class),
        EMPTY("EMPTY", Object.class),
        ROOT("ROOT", UserTask.class),
        CC("CC", ServiceTask.class),
        TRIGGER("TRIGGER", ServiceTask.class),
        DELAY("DELAY", IntermediateCatchEvent.class);
        private String type;

        private Class<?> typeClass;

        Type(String type, Class<?> typeClass) {
            this.type = type;
            this.typeClass = typeClass;
        }

        public final static Map<String, Class<?>> TYPE_MAP = Maps.newHashMap();

        static {
            for (Type element : Type.values()) {
                TYPE_MAP.put(element.type, element.typeClass);
            }
        }

        public boolean isEqual(String type) {
            return this.type.equals(type);
        }

    }


    public static  ChildNode getChildNode(ChildNode childNode,String nodeId){
        Map<String,ChildNode> childNodeMap =new HashMap<>();
        if(StringUtils.isNotBlank(childNode.getId())){
            getChildNode(childNode,childNodeMap);
        }

        Set<String> set = childNodeMap.keySet();
        for (String s : set) {
            if(StringUtils.isNotBlank(s)){
                if(s.equals(nodeId)){
                    return childNodeMap.get(s);
                }
            }
        }
        return null;
    }

    private  static  void getChildNode(ChildNode childNode,Map<String,ChildNode> childNodeMap){
        childNodeMap.put(childNode.getId(),childNode);
        List<ChildNode> branchs = childNode.getBranchs();
        ChildNode children = childNode.getChildren();
        if(branchs!=null && branchs.size()>0){
            for (ChildNode branch : branchs) {
                if(StringUtils.isNotBlank(branch.getId())){
                    childNodeMap.put(branch.getId(),branch);
                    getChildNode(branch,childNodeMap);
                }
            }
        }

        if(children!=null ){
            childNodeMap.put(children.getId(),children);
            getChildNode(children,childNodeMap);
        }

    }

}

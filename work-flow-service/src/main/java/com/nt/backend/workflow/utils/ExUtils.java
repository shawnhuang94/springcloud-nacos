package com.nt.backend.workflow.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.nt.backend.workflow.dto.json.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.impl.de.odysseus.el.ExpressionFactoryImpl;
import org.flowable.common.engine.impl.de.odysseus.el.util.SimpleContext;
import org.flowable.common.engine.impl.javax.el.ExpressionFactory;
import org.flowable.common.engine.impl.javax.el.ValueExpression;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author LoveMyOrange
 * @create 2022-10-16 22:13
 */
@Component
public class ExUtils {
    public String mailContent(DelegateExecution execution,String mailContent){
        byte[] decode = Base64.decode(mailContent.getBytes(Charset.defaultCharset()));
        mailContent=new String(decode);
        Map<String, Object> variables = execution.getVariables();
        Map<String,Object> finalVariables = new HashMap<>();
        finalVariables.putAll(variables);

        RepositoryService repositoryService = SpringContextHolder.getBean(RepositoryService.class);
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(execution.getProcessDefinitionId()).singleResult();
        finalVariables.put("formName",processDefinition.getName());
        finalVariables.put("ownerId","10000");
        finalVariables.put("ownerName","旅人");
        finalVariables.put("ownerDeptId","9999");
        finalVariables.put("ownerDeptName","研发部");
        finalVariables.put("instanceId",execution.getProcessInstanceId());


        ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();
        Set<String> strings = finalVariables.keySet();
        for (String string : strings) {
            context.setVariable(string,factory.createValueExpression(finalVariables.get(string),Object.class));
        }
        ValueExpression valueExpression = factory.createValueExpression(context, mailContent, String.class);
        Object value = valueExpression.getValue(context);
        return (String) value;
    }
    public String requestBody(DelegateExecution execution,String bodyStr){
        byte[] decode = Base64.decode(bodyStr.getBytes(Charset.defaultCharset()));
        bodyStr=new String(decode);
        List<Map> list = JSONObject.parseObject(bodyStr, new TypeReference<List<Map>>() {
        });
        Map<String,Object> bodyMap = new HashMap<>();
        for (Map map : list) {
            Boolean isField = MapUtil.getBool(map, "isField");
            String name = MapUtil.getStr(map, "name");
            String value =MapUtil.getStr(map,"value");
            if(isField){
                String s;
                Object variable = execution.getVariable(value);
                if (variable != null) {
                    s = execution.getVariable(value).toString();
                    bodyMap.put(name,s);
                }
            }
            else{
                if (StringUtils.isNotEmpty(value)) {
                    bodyMap.put(name, value);
                }
            }
        }

        return JSONObject.toJSONString(bodyMap);
    }
    public String requestHeaders(DelegateExecution execution,String headerStr){
        byte[] decode = Base64.decode(headerStr.getBytes(Charset.defaultCharset()));
        headerStr=new String(decode);
        List<Map> list = JSONObject.parseObject(headerStr, new TypeReference<List<Map>>() {
        });
        String headerResultStr="";
        for (Map map : list) {
            Boolean isField = MapUtil.getBool(map, "isField");
            String name = MapUtil.getStr(map, "name");
            String value = MapUtil.getStr(map, "value");
            if (StringUtils.isNotEmpty(name)) {
                headerResultStr += (name + ": ");
            }
            if(isField){
                String s;
                Object variable = execution.getVariable(value);
                if (variable != null) {
                    s = variable.toString();
                    headerResultStr+=s+" \n";
                }
            }
            else{
                if (StringUtils.isNotEmpty(value)) {
                    headerResultStr += value + " \n";
                }
            }
        }
        return headerResultStr;
    }
    public Boolean strEqualsMethod(String controlId,String value){
        List<String> list = Arrays.asList(value);
            String s = list.get(0);
            return s.equals(controlId);
    }
    public Boolean strEqualsMethod(String controlId,String...values){
        List<String> list = Arrays.asList(values);
        if(list.size()>1){
            return Boolean.FALSE;
        }
        else{
            String s = list.get(0);
            return s.equals(controlId);
        }
    }

    public Boolean strContains(String controlId,String...values){
        List<String> list = Arrays.asList(values);
        return list.contains(controlId);
    }

    public Boolean strContains(String controlId,Number...values){
        Long aLong = Long.valueOf(controlId);
        List<Number> numbers = Arrays.asList(values);
        return numbers.contains(aLong);
    }

    public Boolean strContainsMethod(String controlId,String...values){
        List<String> strings = Arrays.asList(values);
        return strings.contains(controlId);
    }


    public Boolean userStrContainsMethod(String controlId,String...values){
        List<String> strings = Arrays.asList(values);
        List<UserInfo> userInfos = JSONObject.parseObject(controlId, new TypeReference<List<UserInfo>>() {
        });
        List<String> idsList= new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            idsList.add(userInfo.getId());
        }
        Collection<String> intersection = CollUtil.intersection(strings, idsList);
        if(CollUtil.isEmpty(intersection)){
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public Boolean deptStrContainsMethod(String controlId,String...values){
        List<String> strings = Arrays.asList(values);
        List<UserInfo> userInfos = JSONObject.parseObject(controlId, new TypeReference<List<UserInfo>>() {
        });
        List<String> idsList= new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            idsList.add(userInfo.getId());
        }
        Collection<String> intersection = CollUtil.intersection(strings, idsList);
        if(CollUtil.isEmpty(intersection)){
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public Boolean numberContains(Number controlId,Number...values){
        List<Number> list = Arrays.asList(values);
        return list.contains(controlId);
    }
    public Boolean b(String controlId,Number...values){
        List<Number> numbers = Arrays.asList(values);
        Long aLong = Long.valueOf(controlId);
        if( aLong> numbers.get(0).longValue()  &&aLong   <numbers.get(1).longValue()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }



    public Boolean ab(String controlId,Number...values){
        List<Number> numbers = Arrays.asList(values);
        Long aLong = Long.valueOf(controlId);
        if(aLong >= numbers.get(0).longValue()  &&aLong   <numbers.get(1).longValue()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
    public Boolean ba(String controlId,Number...values){
        List<Number> numbers = Arrays.asList(values);
        Long aLong = Long.valueOf(controlId);
        if(aLong > numbers.get(0).longValue()  &&aLong   <=numbers.get(1).longValue()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
    public Boolean aba(String controlId,Number...values){
        List<Number> numbers = Arrays.asList(values);
        Long aLong = Long.valueOf(controlId);
        if(aLong >= numbers.get(0).longValue()  && aLong   <=numbers.get(1).longValue()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    /**
     conditionExpression.append(" "+ EXPRESSION_CLASS+"numberEquals("+id+","+str+") " );
     conditionExpression.append(" "+ EXPRESSION_CLASS+"numberGt("+id+","+str+") " );
     conditionExpression.append(" "+ EXPRESSION_CLASS+"numberGtEquals("+id+","+str+") " );
     conditionExpression.append(" "+ EXPRESSION_CLASS+"numberLt("+id+","+str+") " );
     conditionExpression.append(" "+ EXPRESSION_CLASS+"numberLtEquals("+id+","+str+") " );
     */
    public Boolean numberEquals(String controlId,String value){
        Double a = Double.valueOf(controlId);
        Double b = Double.valueOf(value);
        boolean equals = a.equals(b);
        return equals;
    }
    public Boolean numberGt(String controlId,String value){
        Double a = Double.valueOf(controlId);
        BigDecimal a1 = BigDecimal.valueOf(a);
        Double b = Double.valueOf(value);
        BigDecimal a2 = BigDecimal.valueOf(b);
        boolean greater = NumberUtil.isGreater(a1, a2);
        return greater;
    }

    public Boolean numberGtEquals(String controlId,String value){
        Double a = Double.valueOf(controlId);
        BigDecimal a1 = BigDecimal.valueOf(a);
        Double b = Double.valueOf(value);
        BigDecimal a2 = BigDecimal.valueOf(b);
        boolean greater = NumberUtil.isGreaterOrEqual(a1, a2);
        return greater;
    }

    public Boolean numberLt(String controlId,String value){
        Double a = Double.valueOf(controlId);
        BigDecimal a1 = BigDecimal.valueOf(a);
        Double b = Double.valueOf(value);
        BigDecimal a2 = BigDecimal.valueOf(b);
        boolean greater = NumberUtil.isLess(a1, a2);
        return greater;
    }
    public Boolean numberLtEquals(String controlId,String value){
        Double a = Double.valueOf(controlId);
        BigDecimal a1 = BigDecimal.valueOf(a);
        Double b = Double.valueOf(value);
        BigDecimal a2 = BigDecimal.valueOf(b);
        boolean greater = NumberUtil.isLessOrEqual(a1, a2);
        return greater;
    }
}

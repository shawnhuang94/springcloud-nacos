package com.nt.backend.workflow.listener;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.nt.backend.workflow.dto.json.UserInfo;
import com.nt.backend.workflow.entity.Cc;
import com.nt.backend.workflow.service.CcService;
import com.nt.backend.workflow.utils.SpringContextHolder;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.el.FixedValue;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LoveMyOrange
 * @create 2022-10-15 19:47
 */
@Component
public class CcListener implements JavaDelegate {
    FixedValue ccUser;
    @Resource
    private RepositoryService repositoryService;

    @Override
    public void execute(DelegateExecution execution) {
        Object value = ccUser.getValue(execution);
        List<UserInfo> userInfos = JSONObject.parseObject((String) value, new TypeReference<List<UserInfo>>() {
        });
        List<Cc> ccs= new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            if(userInfo.getType().equals("user")){
                Cc cc = new Cc();
                cc.setId(IdWorker.getIdStr());
                cc.setUserId(Long.valueOf(userInfo.getId()));
                cc.setProcessInstanceId(execution.getProcessInstanceId());
                ccs.add(cc);
            }
        }
        CcService ccService = SpringContextHolder.getBean(CcService.class);
        ccService.saveBatch(ccs);
    }
}

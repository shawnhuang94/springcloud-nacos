package com.nt.backend.workflow.job;

import com.nt.backend.workflow.utils.IdWorker;
import com.nt.backend.workflow.utils.SpringContextHolder;
import org.flowable.common.engine.impl.cfg.IdGenerator;
import org.springframework.stereotype.Component;

/**
 * @Author:LoveMyOrange
 * @Description:
 * @Date:Created in 2022/10/17 13:01
 */
@Component
public class IdWorkerIdGenerator implements IdGenerator {

  @Override
  public String getNextId() {
    return SpringContextHolder.getBean(IdWorker.class).nextId()+"";
  }
}

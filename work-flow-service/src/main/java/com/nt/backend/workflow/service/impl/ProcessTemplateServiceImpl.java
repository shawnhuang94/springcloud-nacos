package com.nt.backend.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nt.backend.workflow.entity.ProcessTemplates;
import com.nt.backend.workflow.mapper.ProcessTemplatesMapper;
import com.nt.backend.workflow.service.ProcessTemplateService;
import org.springframework.stereotype.Service;

/**
 * @author : willian fu
 * @version : 1.0
 */
@Service
public class ProcessTemplateServiceImpl extends ServiceImpl<ProcessTemplatesMapper, ProcessTemplates>  implements
        ProcessTemplateService {

}

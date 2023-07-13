package com.nt.backend.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nt.backend.workflow.entity.TemplateGroup;
import com.nt.backend.workflow.mapper.TemplateGroupMapper;
import com.nt.backend.workflow.service.TemplateGroupService;
import org.springframework.stereotype.Service;

/**
 * @author : willian fu
 * @version : 1.0
 */
@Service
public class TemplateGroupServiceImpl extends ServiceImpl<TemplateGroupMapper, TemplateGroup>  implements
        TemplateGroupService {

}

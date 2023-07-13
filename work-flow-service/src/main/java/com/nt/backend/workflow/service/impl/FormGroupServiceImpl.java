package com.nt.backend.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nt.backend.workflow.entity.FormGroups;
import com.nt.backend.workflow.mapper.FormGroupsMapper;
import com.nt.backend.workflow.service.FormGroupService;
import org.springframework.stereotype.Service;

/**
 * @author : willian fu
 * @version : 1.0
 */
@Service
public class FormGroupServiceImpl extends ServiceImpl<FormGroupsMapper, FormGroups>  implements
        FormGroupService {


}

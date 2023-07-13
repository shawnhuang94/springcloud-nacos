package com.nt.backend.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nt.backend.workflow.entity.Departments;
import com.nt.backend.workflow.mapper.DepartmentsMapper;
import com.nt.backend.workflow.service.DepartmentsService;
import org.springframework.stereotype.Service;

/**
 * @author : willian fu
 * @version : 1.0
 */
@Service
public class DepartmentsServiceImpl extends ServiceImpl<DepartmentsMapper, Departments>  implements
        DepartmentsService {

}

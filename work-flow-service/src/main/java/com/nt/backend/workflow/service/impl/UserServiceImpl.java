package com.nt.backend.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nt.backend.workflow.entity.Users;
import com.nt.backend.workflow.mapper.UsersMapper;
import com.nt.backend.workflow.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author : willian fu
 * @version : 1.0
 */
@Service
public class UserServiceImpl extends ServiceImpl<UsersMapper, Users>  implements UserService {

}

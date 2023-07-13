package com.nt.backend.workflow.dto;

import com.nt.backend.workflow.dto.json.UserInfo;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author LoveMyOrange
 * @create 2022-10-14 23:47
 */
@Data
@ApiModel("待办 需要返回给前端的VO")
public class TaskDTO extends PageDTO {
    private UserInfo currentUserInfo;
}

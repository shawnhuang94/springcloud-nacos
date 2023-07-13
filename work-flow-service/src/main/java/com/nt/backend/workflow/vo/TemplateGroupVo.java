package com.nt.backend.workflow.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : willian fu
 * @date : 2020/9/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateGroupVo {

    private Integer id;

    private String name;

    private List<Template> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Template{

        private String forworkflow;

        private Integer tgId;

        private String formName;

        private String icon;

        private Boolean isStop;

        private String remark;
        private JSONObject logo;

        private String background;

        private String updated;
        private String templateId;
        private String processDefinitionId;
    }


}

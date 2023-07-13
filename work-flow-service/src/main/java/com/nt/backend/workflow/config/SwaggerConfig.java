package com.nt.backend.workflow.config;


import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;


@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {

//    @Bean(value = "dockerBean")
//    public Docket dockerBean() {
//        //指定使用Swagger2规范
//        Docket docket=new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(new ApiInfoBuilder()
//                        //描述字段支持Markdown语法
//                        .description("仿钉钉Activiti-Flowable-Camunda")
//                        .termsOfServiceUrl("https://dingding.com/")
//                        .contact("2471089198@qq.com")
//                        .version("1.0")
//                        .build())
//                //分组名称
//                .groupName("工作流服务")
//                .select()
//                //这里指定Controller扫描包路径
//                .apis(RequestHandlerSelectors.basePackage("com.dingding.workflow.controller"))
//                .paths(PathSelectors.any())
//                .build();
//        return docket;
//    }

}

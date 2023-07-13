package com.example.nacosgateway;

import com.alibaba.nacos.common.tls.TlsSystemConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class NacosGatewayApplication {

    public static void main(String[] args) {
//        System.setProperty(TlsSystemConfig.TLS_ENABLE, "true");
        SpringApplication.run(NacosGatewayApplication.class, args);
    }

}

package com.dmos.dmos_clientmachine.bean;

import com.dmos.dmos_client.DMOSClientContext;
import com.dmos.dmos_clientmachine.util.DMOSClientConfig;
import com.dmos.dmos_common.util.ConfigUtil;
import com.dmos.dmos_common.util.HttpUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DMOSBean {
    @Bean
    public static DMOSClientContext dmosClientContext() { return new DMOSClientContext(); }
    @Bean
    public static DMOSClientConfig dmosClientConfig(){
        DMOSClientConfig config = ConfigUtil.load("config.json", DMOSClientConfig.class);
        if(config == null){
            config = new DMOSClientConfig();
            ConfigUtil.save(config, "config.json");
        }
        return config;
    }
    @ConditionalOnMissingBean(RestTemplate.class)
    @Bean
    public static RestTemplate restTemplate(){
        return new RestTemplate();
    }
    @Bean
    public static HttpUtil httpUtil() { return new HttpUtil(); }

}

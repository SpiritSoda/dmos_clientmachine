package com.dmos.dmos_clientmachine.component;

import com.dmos.dmos_client.DMOSClient;
import com.dmos.dmos_client.DMOSClientContext;
import com.dmos.dmos_clientmachine.bean.SpringUtil;
import com.dmos.dmos_clientmachine.handler.DMOSMachineClientHandler;
import com.dmos.dmos_clientmachine.util.DMOSClientConfig;
import com.dmos.dmos_common.config.DMOSConfig;
import com.dmos.dmos_common.data.*;
import com.dmos.dmos_common.data.state.CPU;
import com.dmos.dmos_common.data.state.Ram;
import com.dmos.dmos_common.data.state.Storage;
import com.dmos.dmos_common.message.Message;
import com.dmos.dmos_common.message.MessageType;
import com.dmos.dmos_common.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

@Component
@Slf4j
public class DMOSMachineClient {
    private final HttpUtil httpUtil = SpringUtil.getBean(HttpUtil.class);
    private final DMOSClientConfig dmosConfig = SpringUtil.getBean(DMOSClientConfig.class);
    private final DMOSClientContext clientContext = SpringUtil.getBean(DMOSClientContext.class);
    private final RestTemplate restTemplate = SpringUtil.getBean(RestTemplate.class);

    private static DMOSMachineClient single = null;
    private com.dmos.dmos_client.DMOSClient client;

    @PostConstruct //通过@PostConstruct实现初始化bean之前进行的操作
    public void init() {
        single = this;
        // 初使化时将已静态化的testService实例化
    }

    public static DMOSMachineClient getSingle(){
        return single;
    }
    public void run(){
        new Thread(){
            @Override
            public void run(){
                while(true){
                    try {
                        String token = dmosConfig.getLocalToken();
                        String url = dmosConfig.getRegister() + ":" + Port.REGISTER_HTTP_PORT;
                        HttpHeaders headers = new HttpHeaders();
                        headers.add("token", token);
                        DMOSResponse response = httpUtil.post(url, "/register/token", headers, new DMOSRequest(), restTemplate);
                        if (response.getCode() != 0) {
                            log.info("token无效，注册机器中");
                            DMOSRequest request = new DMOSRequest();
                            NodeDTO nodeDTO = new NodeDTO();
                            nodeDTO.setInterval(dmosConfig.getInterval());
                            nodeDTO.setType(NodeType.CLIENT);
                            nodeDTO.setName(dmosConfig.getName());
                            request.put("info", nodeDTO);
                            DMOSResponse register = httpUtil.post(url, "/register/register", headers, request, restTemplate);
                            if (register.getCode() != 0) {
                                log.error("无法获取token");
                                return;
                            }
                            token = (String) register.getData().get("token");
//                    log.info(token);
                            dmosConfig.setLocalToken(token);
                            ConfigUtil.save(dmosConfig, "config.json");
                            headers.set("token", token);
                            response = httpUtil.post(url, "/register/token", headers, new DMOSRequest(), restTemplate);
                        }
                        int id = (Integer) response.getData().get("id");
                        clientContext.id(id);
                        clientContext.token((String) response.getData().get("token"));
                    } catch (Exception e) {
                        log.error("连接注册服务器出错");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            e.printStackTrace();
                        }

                        continue;
                    }

                    DMOSClient client = new com.dmos.dmos_client.DMOSClient(new InetSocketAddress(dmosConfig.getSocketIP(), Port.SOCKET_CHANNEL_PORT), new DMOSMachineClientHandler());
                    client.connect();

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    @Scheduled(fixedRate = 10000)
    public void heartbeat() {
        log.info("正在发送心跳");
        ChannelUtil.heartbeat(clientContext.getParent());
    }
}

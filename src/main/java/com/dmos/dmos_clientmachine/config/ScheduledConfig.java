package com.dmos.dmos_clientmachine.config;

import com.dmos.dmos_client.DMOSClientContext;
import com.dmos.dmos_clientmachine.bean.SpringUtil;
import com.dmos.dmos_clientmachine.util.DMOSClientConfig;
import com.dmos.dmos_common.data.ClientReportDTO;
import com.dmos.dmos_common.data.state.CPU;
import com.dmos.dmos_common.data.state.Ram;
import com.dmos.dmos_common.data.state.Storage;
import com.dmos.dmos_common.message.Message;
import com.dmos.dmos_common.message.MessageType;
import com.dmos.dmos_common.util.ParseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduledConfig implements SchedulingConfigurer {
    private final DMOSClientConfig dmosConfig = SpringUtil.getBean(DMOSClientConfig.class);
    private final DMOSClientContext clientContext = SpringUtil.getBean(DMOSClientContext.class);
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addFixedRateTask(
                new Runnable() {
                    @Override
                    public void run() {
                        log.info("正在报告状态");
                        ClientReportDTO reportDTO = new ClientReportDTO();
                        reportDTO.setId(clientContext.getId());
                        reportDTO.setTimestamp(System.currentTimeMillis() / 1000L);
                        double cpu_percent = 0.5 + Math.random() / 5;
                        double ram_percent = 0.5 + Math.random() / 5;
                        double disk_percent = 0.4 + Math.random() / 4;
                        reportDTO.setCpu(new CPU(cpu_percent, 12 * cpu_percent, 12));
                        reportDTO.setRam(new Ram(ram_percent, 32 * ram_percent, 32));
                        reportDTO.setStorage(new Storage(disk_percent, 512 * disk_percent, 512));
                        Message message = new Message();
                        message.setType(MessageType.CLIENT_REPORT);
                        message.setData(ParseUtil.encode(reportDTO, false));
                        clientContext.send(message);
                    }
                },
                dmosConfig.getInterval()
        );
    }
}

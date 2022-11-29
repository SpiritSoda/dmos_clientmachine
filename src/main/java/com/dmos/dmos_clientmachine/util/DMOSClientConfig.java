package com.dmos.dmos_clientmachine.util;

import com.dmos.dmos_common.config.DMOSConfig;
import lombok.Data;

@Data
public class DMOSClientConfig extends DMOSConfig {
    // 只在初始化时有作用，机器一旦注册除非用户修改否则不可变更
    private String name;
    // 必须设置
    private int interval;
    public DMOSClientConfig(){
        super();
        this.interval = 10000;
        this.name = "";
    }
}

package com.dmos.dmos_clientmachine.handler;

import com.dmos.dmos_client.DMOSClientContext;
import com.dmos.dmos_clientmachine.bean.SpringUtil;
import com.dmos.dmos_common.data.ConfigDTO;
import com.dmos.dmos_common.message.Message;
import com.dmos.dmos_common.message.MessageType;
import com.dmos.dmos_common.util.ParseUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DMOSMachineClientHandler extends ChannelInboundHandlerAdapter {
    private final DMOSClientContext clientContext = SpringUtil.getBean(DMOSClientContext.class);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("父节点通道已建立: {}", ctx.channel().id().asLongText());
        // send verify token
        clientContext.channel(ctx.channel());
        Message verifyMessage = new Message();
        verifyMessage.setType(MessageType.IDENTIFY);
        verifyMessage.setData(clientContext.getToken());
        clientContext.send(verifyMessage);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Todo：奇怪的问题，可能是线程冲突导致的消息叠加
        String[] msgs = msg.toString().split("\r");
        for(String s: msgs){
            log.info("从父节点通道 {} 中收到信息: {}", ctx.channel().id().asLongText(), s);
            Message message = ParseUtil.decode(s, Message.class);
            if(message.getType() == MessageType.CONFIG){
                ConfigDTO configDTO = ParseUtil.decode(message.getData(), ConfigDTO.class);
                int client = configDTO.getId();
            }
            else if(message.getType() == MessageType.HEARTBEAT){

            }
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("父节点通道 {} 出现异常", ctx.channel().id().asLongText());
//        cause.printStackTrace();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.warn("父节点通道 {} 关闭", ctx.channel().id().asLongText());
        clientContext.channel(null);
    }

}

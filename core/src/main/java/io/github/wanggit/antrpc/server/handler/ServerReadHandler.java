package io.github.wanggit.antrpc.server.handler;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.commons.bean.*;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializerHolder;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.server.invoker.IRpcRequestBeanInvoker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerReadHandler extends SimpleChannelInboundHandler<RpcProtocol> {

    private IRpcRequestBeanInvoker rpcRequestBeanInvoker;
    private ISerializerHolder serializerHolder;

    public ServerReadHandler(
            IRpcRequestBeanInvoker rpcRequestBeanInvoker, ISerializerHolder serializerHolder) {
        this.rpcRequestBeanInvoker = rpcRequestBeanInvoker;
        this.serializerHolder = serializerHolder;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol msg) throws Exception {
        if (msg.getType() == ConstantValues.HB_TYPE) {
            RpcProtocol rpcProtocol = HeartBeatCreator.create(msg.getCmdId());
            ctx.channel().writeAndFlush(rpcProtocol);
        } else {
            RpcRequestBean requestBean =
                    (RpcRequestBean) serializerHolder.getSerializer().deserialize(msg.getData());
            if (log.isDebugEnabled()) {
                log.debug(JSONObject.toJSONString(requestBean));
            }
            SerialNumberThreadLocal.TraceEntity traceEntity =
                    new SerialNumberThreadLocal.TraceEntity();
            traceEntity.setSerialNumber(requestBean.getSerialNumber());
            traceEntity.setCaller(requestBean.getId());
            SerialNumberThreadLocal.set(traceEntity);
            RpcResponseBean bean = rpcRequestBeanInvoker.invoke(requestBean);
            if (!requestBean.isOneway()) {
                RpcProtocol protocol = new RpcProtocol();
                protocol.setCmdId(msg.getCmdId());
                protocol.setType(msg.getType());
                protocol.setData(serializerHolder.getSerializer().serialize(bean));
                ctx.channel().writeAndFlush(protocol);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (log.isErrorEnabled()) {
            log.error(cause.getMessage());
        }
        ctx.close();
    }
}

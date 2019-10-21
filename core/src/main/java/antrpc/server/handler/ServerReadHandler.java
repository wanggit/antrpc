package antrpc.server.handler;

import antrpc.commons.bean.RpcProtocol;
import antrpc.commons.bean.RpcRequestBean;
import antrpc.commons.bean.RpcResponseBean;
import antrpc.commons.bean.SerialNumberThreadLocal;
import antrpc.commons.codec.kryo.KryoSerializer;
import antrpc.commons.constants.ConstantValues;
import antrpc.server.invoker.IRpcRequestBeanInvoker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerReadHandler extends SimpleChannelInboundHandler<RpcProtocol> {

    private IRpcRequestBeanInvoker rpcRequestBeanInvoker;

    public ServerReadHandler(IRpcRequestBeanInvoker rpcRequestBeanInvoker) {
        this.rpcRequestBeanInvoker = rpcRequestBeanInvoker;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol msg) throws Exception {
        if (msg.getType() == ConstantValues.HB_TYPE) {
            if (log.isDebugEnabled()) {
                log.debug("Heartbeat received from [" + ctx.channel() + "]");
            }
            return;
        }

        RpcRequestBean requestBean =
                (RpcRequestBean) KryoSerializer.getInstance().deserialize(msg.getData());

        SerialNumberThreadLocal.TraceEntity traceEntity = new SerialNumberThreadLocal.TraceEntity();
        traceEntity.setSerialNumber(requestBean.getSerialNumber());
        traceEntity.setCaller(requestBean.getId());
        SerialNumberThreadLocal.set(traceEntity);
        RpcResponseBean bean = rpcRequestBeanInvoker.invoke(requestBean);
        if (!requestBean.isOneway()) {
            RpcProtocol protocol = new RpcProtocol();
            protocol.setCmdId(msg.getCmdId());
            protocol.setType(msg.getType());
            protocol.setData(KryoSerializer.getInstance().serialize(bean));
            ctx.channel().writeAndFlush(protocol);
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

package io.github.wanggit.antrpc.server.telnet;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.commons.config.TelnetConfig;
import io.github.wanggit.antrpc.commons.utils.EpollUtil;
import io.github.wanggit.antrpc.server.telnet.handler.CmdDesc;
import io.github.wanggit.antrpc.server.telnet.handler.TelnetServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class TelnetServer implements ITelnetServer {

    private MultithreadEventLoopGroup bossGroup = EpollUtil.newEventLoopGroupInstance();
    private MultithreadEventLoopGroup workerGroup = EpollUtil.newEventLoopGroupInstance();
    private Channel channel;
    private IAntrpcContext antrpcContext;
    private ConfigurableApplicationContext applicationContext;

    public TelnetServer(
            IAntrpcContext antrpcContext, ConfigurableApplicationContext applicationContext) {
        this.antrpcContext = antrpcContext;
        this.applicationContext = applicationContext;
    }

    @Override
    public void start(TelnetConfig telnetConfig)
            throws InterruptedException, IOException, ClassNotFoundException {
        Map<String, CmdInfoBean> telnetCmds = collectCmds();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(EpollUtil.serverSocketChannelClass())
                .childHandler(
                        new ChannelInitializer() {
                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                ch.pipeline()
                                        .addLast(
                                                new DelimiterBasedFrameDecoder(
                                                        4096, Delimiters.lineDelimiter()))
                                        .addLast(new StringDecoder())
                                        .addLast(new StringEncoder())
                                        .addLast(
                                                new TelnetServerHandler(
                                                        telnetConfig, antrpcContext, telnetCmds));
                            }
                        })
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        channel = serverBootstrap.bind(telnetConfig.getPort()).sync().channel();
        if (log.isInfoEnabled()) {
            log.info(
                    "TelnetServer initialization complete, listen on port "
                            + telnetConfig.getPort());
        }
    }

    private Map<String, CmdInfoBean> collectCmds() throws IOException, ClassNotFoundException {
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(applicationContext);
        Resource[] resources =
                applicationContext.getResources(
                        "classpath*:io/github/wanggit/antrpc/server/telnet/handler/command/*.class");
        Map<String, CmdInfoBean> cmds = new HashMap<>();
        for (Resource r : resources) {
            MetadataReader reader = metaReader.getMetadataReader(r);
            Set<String> annotationTypes = reader.getAnnotationMetadata().getAnnotationTypes();
            if (annotationTypes.contains(CmdDesc.class.getName())) {
                Class aClass = Class.forName(reader.getClassMetadata().getClassName());
                CmdDesc cmdDesc = AnnotationUtils.findAnnotation(aClass, CmdDesc.class);
                CmdInfoBean cmdInfoBean = new CmdInfoBean();
                cmdInfoBean.setAClass(aClass);
                cmdInfoBean.setDesc(cmdDesc.desc());
                cmdInfoBean.setValue(cmdDesc.value());
                cmds.put(cmdDesc.value(), cmdInfoBean);
            }
        }
        return cmds;
    }

    @Override
    public void close() {
        if (log.isInfoEnabled()) {
            log.info("TelnetServer is being destroyed.");
        }
        channel.close();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}

package io.github.wanggit.antrpc.monitor.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class ElasticsearchConfiguration {

    @Bean(destroyMethod = "close")
    public TransportClient client() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", "docker-cluster").build();
        TransportClient client =
                new PreBuiltTransportClient(settings)
                        .addTransportAddress(
                                new TransportAddress(
                                        InetAddress.getByName("192.168.14.132"), 9300));

        return client;
    }
}

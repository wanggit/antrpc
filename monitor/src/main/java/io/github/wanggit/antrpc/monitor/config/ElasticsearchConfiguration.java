package io.github.wanggit.antrpc.monitor.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfiguration {

    /*@Autowired private JestProperties jestProperties;

    @Autowired private ElasticsearchProperties elasticsearchProperties;

    @Bean(destroyMethod = "close")
    public TransportClient client() throws UnknownHostException {
        Settings settings =
                Settings.builder()
                        .put("cluster.name", elasticsearchProperties.getClusterName())
                        .build();
        List<String> uris = jestProperties.getUris();
        List<TransportAddress> addresses = new ArrayList<>();
        for (String uri : uris) {
            String ip = uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf(":"));
            int port = Integer.parseInt(uri.substring(uri.lastIndexOf(":") + 1));
            addresses.add(new TransportAddress(InetAddress.getByName(ip), port));
        }
        return new PreBuiltTransportClient(settings)
                .addTransportAddresses(addresses.toArray(new TransportAddress[] {}));
    }*/
}

package io.github.wanggit.antrpc.monitor.elasticsearch.parser;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.monitor.elasticsearch.AbstractElasticsearchEntity;
import io.github.wanggit.antrpc.monitor.elasticsearch.annotations.Document;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ElasticsearchUtils {

    @Autowired private TransportClient client;

    private final ConcurrentHashMap<Class<? extends AbstractElasticsearchEntity>, EntityConfig>
            cache = new ConcurrentHashMap<>();

    public BulkRequestBuilder bulkIndexRequest(
            List<? extends AbstractElasticsearchEntity> entities) {
        if (null == entities || entities.isEmpty()) {
            throw new IllegalArgumentException("The collection cannot be empty or null.");
        }
        BulkRequestBuilder prepareBulk = client.prepareBulk();
        for (AbstractElasticsearchEntity entity : entities) {
            prepareBulk.add(indexRequest(entity));
        }
        return prepareBulk;
    }

    private IndexRequestBuilder indexRequest(AbstractElasticsearchEntity entity) {
        if (null == entity) {
            throw new IllegalArgumentException("Argument cannot be empty or null.");
        }
        EntityConfig entityConfig = getEntityConfig(entity.getClass());
        return client.prepareIndex(
                        entityConfig.getIndexName(), entityConfig.getType(), entity.getId())
                .setSource(JSONObject.toJSONString(entity), XContentType.JSON);
    }

    private EntityConfig getEntityConfig(Class<? extends AbstractElasticsearchEntity> clazz) {
        if (!cache.containsKey(clazz)) {
            synchronized (clazz) {
                if (!cache.containsKey(clazz)) {
                    Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
                    if (null == document) {
                        throw new IllegalStateException(
                                "The "
                                        + clazz.getName()
                                        + " object does not have "
                                        + Document.class.getName()
                                        + " annotations");
                    }
                    EntityConfig entityConfig = new EntityConfig();
                    entityConfig.setCreateIndex(document.createIndex());
                    entityConfig.setIndexName(document.indexName());
                    entityConfig.setIndexStoreType(document.indexStoreType());
                    entityConfig.setRefreshInterval(document.refreshInterval());
                    entityConfig.setReplicas(document.replicas());
                    entityConfig.setShards(document.shards());
                    entityConfig.setType(document.type());
                    entityConfig.setUseServerConfiguration(document.useServerConfiguration());
                    entityConfig.setVersionType(document.versionType());
                    cache.put(clazz, entityConfig);
                }
            }
        }
        return cache.get(clazz);
    }

    private static class EntityConfig {
        private String indexName;
        private String type;
        private boolean useServerConfiguration;
        private int shards;
        private int replicas;
        private String refreshInterval;
        private String indexStoreType;
        private boolean createIndex;
        private VersionType versionType;

        String getIndexName() {
            return indexName;
        }

        void setIndexName(String indexName) {
            this.indexName = indexName;
        }

        String getType() {
            return type;
        }

        void setType(String type) {
            this.type = type;
        }

        public boolean isUseServerConfiguration() {
            return useServerConfiguration;
        }

        void setUseServerConfiguration(boolean useServerConfiguration) {
            this.useServerConfiguration = useServerConfiguration;
        }

        public int getShards() {
            return shards;
        }

        void setShards(int shards) {
            this.shards = shards;
        }

        public int getReplicas() {
            return replicas;
        }

        void setReplicas(int replicas) {
            this.replicas = replicas;
        }

        public String getRefreshInterval() {
            return refreshInterval;
        }

        void setRefreshInterval(String refreshInterval) {
            this.refreshInterval = refreshInterval;
        }

        public String getIndexStoreType() {
            return indexStoreType;
        }

        void setIndexStoreType(String indexStoreType) {
            this.indexStoreType = indexStoreType;
        }

        public boolean isCreateIndex() {
            return createIndex;
        }

        void setCreateIndex(boolean createIndex) {
            this.createIndex = createIndex;
        }

        VersionType getVersionType() {
            return versionType;
        }

        void setVersionType(VersionType versionType) {
            this.versionType = versionType;
        }
    }
}

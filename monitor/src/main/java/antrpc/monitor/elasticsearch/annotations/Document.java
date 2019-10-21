package antrpc.monitor.elasticsearch.annotations;

import org.elasticsearch.index.VersionType;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Document {
    String indexName();

    String type() default "_doc";

    boolean useServerConfiguration() default false;

    short shards() default 5;

    short replicas() default 1;

    String refreshInterval() default "1s";

    String indexStoreType() default "fs";

    boolean createIndex() default true;

    VersionType versionType() default VersionType.EXTERNAL;
}

package io.github.wanggit.antrpc.commons.codec.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

@Slf4j
class KryoPool {

    private static final int DEFAULT_TIMEOUT = 5000;

    KryoPool(GenericObjectPoolConfig<Kryo> config) {
        pool = new GenericObjectPool<Kryo>(new PooledKryoFactory(), config);
    }

    private Kryo borrow(long timeout) {
        try {
            return pool.borrowObject(timeout);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to getId a Kryo object from the Kryo pool.", e);
            }
            return null;
        }
    }

    Kryo borrow() {
        return borrow(DEFAULT_TIMEOUT);
    }

    void returnObject(Kryo kryo) {
        pool.returnObject(kryo);
    }

    private GenericObjectPool<Kryo> pool;
}

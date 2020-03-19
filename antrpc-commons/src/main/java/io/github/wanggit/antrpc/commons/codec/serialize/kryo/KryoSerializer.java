package io.github.wanggit.antrpc.commons.codec.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.Map;

@Slf4j
public class KryoSerializer implements ISerializer {

    private static final String MAX_IDLE = "max_idle";
    private static final String MAX_TOTAL = "max_total";
    private static final String MIN_IDLE = "min_idle";
    private static final String BUFFER_SIZE = "buffer_size";
    private static final String MAX_BUFFER_SIZE = "max_buffer_size";

    private KryoPool kryoPool;

    private Map<String, String> configs;

    private int bufferSize = 4096;

    private int maxBufferSize = 65535;

    @Override
    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    @Override
    public void init() {
        GenericObjectPoolConfig<Kryo> config = new GenericObjectPoolConfig<Kryo>();
        if (null != configs && !configs.isEmpty()) {
            config.setMaxIdle(
                    NumberUtils.toInt(
                            configs.get(MAX_IDLE), GenericObjectPoolConfig.DEFAULT_MAX_IDLE));
            config.setMaxTotal(
                    NumberUtils.toInt(
                            configs.get(MAX_TOTAL), GenericObjectPoolConfig.DEFAULT_MAX_TOTAL));
            config.setMinIdle(
                    NumberUtils.toInt(
                            configs.get(MIN_IDLE), GenericObjectPoolConfig.DEFAULT_MIN_IDLE));
            this.bufferSize = NumberUtils.toInt(configs.get(BUFFER_SIZE), bufferSize);
            this.maxBufferSize = NumberUtils.toInt(configs.get(MAX_BUFFER_SIZE), maxBufferSize);
        }
        this.kryoPool = new KryoPool(config);
    }

    @Override
    public byte[] serialize(Object object) {
        Kryo kryo = kryoPool.borrow();
        try (Output output = new Output(bufferSize, maxBufferSize)) {
            kryo.writeClassAndObject(output, object);
            return output.toBytes();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Kryo serialize ERROR.", e);
            }
        } finally {
            kryoPool.returnObject(kryo);
        }
        return null;
    }

    @Override
    public Object deserialize(byte[] buf) {
        Kryo kryo = kryoPool.borrow();
        try {
            if (buf == null) {
                return null;
            }
            Input input = new Input(buf);
            return kryo.readClassAndObject(input);
        } finally {
            kryoPool.returnObject(kryo);
        }
    }
}

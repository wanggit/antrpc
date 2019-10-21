package antrpc.commons.codec.kryo;

import antrpc.commons.codec.ISerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

@Slf4j
public class KryoSerializer implements ISerializer {
    private KryoPool kryoPool;
    private static KryoSerializer instance = null;

    private KryoSerializer() {}

    public static KryoSerializer getInstance() {
        if (null == instance) {
            synchronized (KryoSerializer.class) {
                if (null == instance) {
                    GenericObjectPoolConfig<Kryo> config = new GenericObjectPoolConfig<Kryo>();
                    config.setMaxIdle(10);
                    config.setMaxTotal(20);
                    config.setMinIdle(8);
                    KryoPool kryoPool = new KryoPool(config);
                    instance = new KryoSerializer();
                    instance.kryoPool = kryoPool;
                }
            }
        }
        return instance;
    }

    @Override
    public byte[] serialize(Object object) {
        Kryo kryo = kryoPool.borrow();
        Output output = new Output(4096, 65535);
        try {
            kryo.writeClassAndObject(output, object);
            return output.toBytes();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Kryo serialize ERROR.", e);
            }

        } finally {
            kryoPool.returnObject(kryo);
            output.close();
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

package antrpc.commons.codec;

public interface ISerializer {
    byte[] serialize(Object object);

    Object deserialize(byte[] buf);
}

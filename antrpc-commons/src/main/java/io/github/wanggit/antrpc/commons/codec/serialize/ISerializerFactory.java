package io.github.wanggit.antrpc.commons.codec.serialize;

public interface ISerializerFactory {

    ISerializer createNewSerializerByByteCmd(byte cmd);
}

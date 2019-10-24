package io.github.wanggit.antrpc.client.zk.register;

public interface IZkRegisterHolder {
    void add(RegisterBean registerBean);

    void allReRegister();
}

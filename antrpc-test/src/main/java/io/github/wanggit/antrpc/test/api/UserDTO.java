package io.github.wanggit.antrpc.test.api;

import lombok.ToString;

import java.io.Serializable;

@ToString
public class UserDTO implements Serializable {

    private static final long serialVersionUID = -449725260187805046L;
    private Long id;
    private String name;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

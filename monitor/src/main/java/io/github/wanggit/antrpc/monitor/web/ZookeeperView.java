package io.github.wanggit.antrpc.monitor.web;

import com.alibaba.fastjson.JSONObject;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.zk.zknode.NodeHostEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

@Route(value = "zookeeper", layout = MainView.class)
public class ZookeeperView extends VerticalLayout {
    private static final long serialVersionUID = -6886754960956235051L;

    public ZookeeperView(@Autowired ApplicationContext applicationContext) {
        IAntrpcContext antrpcContext = applicationContext.getBean(IAntrpcContext.class);
        Map<String, List<NodeHostEntity>> snapshot =
                antrpcContext.getNodeHostContainer().snapshot();
        add(new H2("Zookeeper"), new Label(JSONObject.toJSONString(snapshot)));
    }
}

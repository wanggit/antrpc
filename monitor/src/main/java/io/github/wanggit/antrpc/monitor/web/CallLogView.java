package io.github.wanggit.antrpc.monitor.web;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "calllog", layout = MainView.class)
public class CallLogView extends VerticalLayout {

    private static final long serialVersionUID = 7268251554264907293L;

    public CallLogView() {
        add(new H2("Call Logs"));
    }
}

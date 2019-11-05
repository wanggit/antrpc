package io.github.wanggit.antrpc.monitor.web;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@Route
public class MainView extends AppLayout {

    private static final long serialVersionUID = 567239612811613652L;

    public MainView(@Autowired ApplicationContext context) {
        addToNavbar(new DrawerToggle(), new Label("AntRPC Monitor"));
        VerticalLayout menus = new VerticalLayout();
        menus.add(
                new RouterLink("Zookeeper", ZookeeperView.class),
                new RouterLink("调用日志", CallLogView.class));
        addToDrawer(menus);

        /*pages.put(appTab, new AppView(applicationContext));
        pages.put(scenarioTab, new ScenarioView(applicationContext));
        pages.put(ruleTab, new RuleView(applicationContext));
        pages.put(ruleLogTab, new RuleLogView(applicationContext));*/

        /*setContent(pages.get(appTab));*/
        /*tabs.addSelectedChangeListener(
        event -> {
            Component selectedPage = pages.get(tabs.getSelectedTab());
            setContent(selectedPage);
        });*/
    }
}

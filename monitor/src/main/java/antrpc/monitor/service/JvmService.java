package antrpc.monitor.service;

import antrpc.monitor.domain.Jvm;
import antrpc.monitor.web.vo.LineChartVO;

import java.util.List;

public interface JvmService {
    void save(List<Jvm> jvms);

    LineChartVO<Double> statJvmHeap(String appName, Long start, Long end);

    List<String> lastDayReportedAppNames();
}

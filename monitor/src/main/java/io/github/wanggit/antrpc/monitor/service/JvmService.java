package io.github.wanggit.antrpc.monitor.service;

import io.github.wanggit.antrpc.monitor.domain.Jvm;
import io.github.wanggit.antrpc.monitor.web.vo.LineChartVO;

import java.util.List;

public interface JvmService {
    void save(List<Jvm> jvms);

    LineChartVO<Double> statJvmHeap(String appName, Long start, Long end);

    List<String> lastDayReportedAppNames();
}

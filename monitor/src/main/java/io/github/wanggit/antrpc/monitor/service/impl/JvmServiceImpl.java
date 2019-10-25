package io.github.wanggit.antrpc.monitor.service.impl;

import io.github.wanggit.antrpc.monitor.domain.Jvm;
import io.github.wanggit.antrpc.monitor.elasticsearch.parser.ElasticsearchUtils;
import io.github.wanggit.antrpc.monitor.service.JvmService;
import io.github.wanggit.antrpc.monitor.web.vo.LineChartVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.action.bulk.BulkResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JvmServiceImpl implements JvmService {

    @Autowired private ElasticsearchUtils elasticsearchUtils;

    @Override
    public void save(List<Jvm> jvms) {
        BulkResponse bulkResponse = elasticsearchUtils.bulkIndexRequest(jvms).get();
        if (null != bulkResponse && bulkResponse.hasFailures()) {
            if (log.isErrorEnabled()) {
                log.error(
                        "An exception occurred while saving Jvms to Elasticsearch. "
                                + bulkResponse.buildFailureMessage());
            }
        }
    }

    @Override
    public LineChartVO<Double> statJvmHeap(String appName, Long start, Long end) {
        return null;
        /*List<Jvm> heapCommitteds =
                jvmRepository.findAllByAppNameAndAttrNameAndTsBetween(
                        appName, "jvm.memory.heap.committed", start, end);
        List<Jvm> heapInits =
                jvmRepository.findAllByAppNameAndAttrNameAndTsBetween(
                        appName, "jvm.memory.heap.init", start, end);
        List<Jvm> heapMaxes =
                jvmRepository.findAllByAppNameAndAttrNameAndTsBetween(
                        appName, "jvm.memory.heap.max", start, end);
        List<Jvm> heapUsedes =
                jvmRepository.findAllByAppNameAndAttrNameAndTsBetween(
                        appName, "jvm.memory.heap.used", start, end);
        LineChartVO<Double> jvmHeap = new LineChartVO<>();
        jvmHeap.setTitle(appName);
        jvmHeap.setLegends(
                Lists.newArrayList("heap.committed", "heap.init", "heap.max", "heap.used"));
        Map<String, Boolean> selectedLegends = new HashMap<>();
        selectedLegends.put("heap.committed", true);
        selectedLegends.put("heap.init", true);
        selectedLegends.put("heap.max", false);
        selectedLegends.put("heap.used", true);
        jvmHeap.setSelectedLegends(selectedLegends);
        List<String> xAxies = new ArrayList<>(heapCommitteds.size());
        List<LineChartVO.SeriesData<Double>> series = new ArrayList<>(jvmHeap.getLegends().size());

        LineChartVO.SeriesData<Double> committedSeriesData = new LineChartVO.SeriesData<>();
        committedSeriesData.setName("heap.committed");
        committedSeriesData.setType("line");
        List<Double> committeds = new ArrayList<>(heapCommitteds.size());
        heapCommitteds.forEach(
                it -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd HH:mm:ss");
                    String xAxis = sdf.format(new Date(it.getTs()));
                    xAxies.add(xAxis);
                    long value = NumberUtils.toLong(it.getAttrValue(), 0);
                    committeds.add(
                            new BigDecimal(value / 1024.0D / 1024.0D) // MB
                                    .setScale(2, RoundingMode.FLOOR)
                                    .doubleValue());
                });
        committedSeriesData.setData(committeds);
        series.add(committedSeriesData);

        LineChartVO.SeriesData<Double> initSeriesData =
                createSeriesData(heapInits, "heap.init", "line");
        series.add(initSeriesData);
        LineChartVO.SeriesData<Double> maxSeriesData =
                createSeriesData(heapMaxes, "heap.max", "line");
        series.add(maxSeriesData);
        LineChartVO.SeriesData<Double> usedSeriesData =
                createSeriesData(heapUsedes, "heap.used", "line");
        series.add(usedSeriesData);

        jvmHeap.setSeries(series);
        jvmHeap.setxAxies(xAxies);
        return jvmHeap;*/
    }

    @Override
    public List<String> lastDayReportedAppNames() {
        return null;
    }

    private LineChartVO.SeriesData<Double> createSeriesData(
            List<Jvm> heapInits, String name, String type) {
        LineChartVO.SeriesData<Double> seriesData = new LineChartVO.SeriesData<>();
        seriesData.setName(name);
        seriesData.setType(type);
        List<Double> values = new ArrayList<>(heapInits.size());
        heapInits.forEach(
                it -> {
                    long value = NumberUtils.toLong(it.getAttrValue(), 0);
                    values.add(
                            new BigDecimal(value / 1024.0D / 1024.0D) // MB
                                    .setScale(2, RoundingMode.FLOOR)
                                    .doubleValue());
                });
        seriesData.setData(values);
        return seriesData;
    }
}

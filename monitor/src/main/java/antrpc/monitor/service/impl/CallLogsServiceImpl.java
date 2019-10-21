package antrpc.monitor.service.impl;

import antrpc.commons.bean.RpcCallLog;
import antrpc.monitor.domain.CallLogs;
import antrpc.monitor.elasticsearch.parser.ElasticsearchUtils;
import antrpc.monitor.service.CallLogsService;
import antrpc.monitor.service.dto.LastHourStatDTO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CallLogsServiceImpl implements CallLogsService {

    @Autowired private ElasticsearchUtils elasticsearchUtils;

    @Override
    public void save(List<RpcCallLog> rpcCallLogs) {
        int len = rpcCallLogs.size();
        List<CallLogs> entities = new ArrayList<>(len + len >> 1);
        rpcCallLogs.forEach(
                it -> {
                    CallLogs callLogs = new CallLogs();
                    BeanUtils.copyProperties(it, callLogs);
                    entities.add(callLogs);
                });
        BulkResponse response = elasticsearchUtils.bulkIndexRequest(entities).get();
        if (null != response && response.hasFailures()) {
            if (log.isErrorEnabled()) {
                log.error(
                        "An exception occurred while saving rpcCallLog to Elasticsearch. "
                                + response.buildFailureMessage());
            }
        }
    }

    private final String rpcCallLogLastHour_sql =
            "SELECT COUNT(a.id) AS callCount, ROUND(IFNULL(AVG(IFNULL(a.rt, 0)), 0)) AS avgRt \n"
                    + "FROM rpc_monitor.call_logs a \n"
                    + "WHERE a.`start` >= :start \n"
                    + "AND a.`start` <= :end";

    @Override
    public LastHourStatDTO rpcCallLogLastHour() {
        return new LastHourStatDTO();
    }
}

package antrpc.monitor.service;

import io.github.wanggit.antrpc.commons.bean.RpcCallLog;
import antrpc.monitor.service.dto.LastHourStatDTO;

import java.util.List;

public interface CallLogsService {
    void save(List<RpcCallLog> rpcCallLogs);

    LastHourStatDTO rpcCallLogLastHour();
}

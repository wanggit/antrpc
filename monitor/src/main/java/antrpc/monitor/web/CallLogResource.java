package antrpc.monitor.web;

import antrpc.monitor.domain.CallLogs;
import antrpc.monitor.service.CallLogsService;
import antrpc.monitor.web.vm.CallLogVM;
import antrpc.monitor.web.vo.Page;
import antrpc.monitor.web.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/calllogs")
public class CallLogResource {

    @Autowired private CallLogsService callLogsService;

    private static final String queryLogs_sql =
            "SELECT a.id, a.class_name AS className, a.method_name as methodName, a.`start`\n"
                    + "\t, a.`end`, a.rt, a.error_message as errorMessage, a.caller\n"
                    + "\t, a.request_id as requestId, a.serial_number as serialNumber, a.ip, a.`port`\n"
                    + "FROM rpc_monitor.call_logs a \n"
                    + "WHERE 1 = 1\n"
                    + "AND a.class_name LIKE :className \n"
                    + "AND a.`start` > :start \n"
                    + "AND a.`start` < :end \n"
                    + "ORDER BY a.`start` DESC  LIMIT :offset, :size";
    private static final String queryLogs_sql_count =
            "SELECT COUNT(a.id) AS cnt\n"
                    + "FROM rpc_monitor.call_logs a \n"
                    + "WHERE 1 = 1\n"
                    + "AND a.class_name LIKE :className \n"
                    + "AND a.`start` > :start \n"
                    + "AND a.`start` < :end ";

    /**
     * 依据时间戳范围与类名查询
     *
     * @param callLogVM
     * @return
     */
    @PostMapping("/query")
    public Result<Page<CallLogs>> queryLogs(@RequestBody CallLogVM callLogVM) {
        return null;
    }

    @GetMapping("/query")
    public Result<ArrayList<CallLogs>> queryLogs(
            @RequestParam(value = "serialNumber") String serialNumber) {
        return null;
    }
}

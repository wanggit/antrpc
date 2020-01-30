package io.github.wanggit.antrpc.client.monitor.report;

import io.github.wanggit.antrpc.commons.bean.RpcCallLog;
import io.github.wanggit.antrpc.commons.config.IConfiguration;

public interface ILogReporter {

    void report(RpcCallLog log);

    void setConfiguration(IConfiguration configuration);

    IConfiguration getConfiguration();
}

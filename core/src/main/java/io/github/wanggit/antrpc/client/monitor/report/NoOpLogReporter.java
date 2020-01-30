package io.github.wanggit.antrpc.client.monitor.report;

import io.github.wanggit.antrpc.commons.bean.RpcCallLog;
import io.github.wanggit.antrpc.commons.config.IConfiguration;

public class NoOpLogReporter implements ILogReporter {
    @Override
    public void report(RpcCallLog log) {}

    @Override
    public void setConfiguration(IConfiguration configuration) {}

    @Override
    public IConfiguration getConfiguration() {
        return null;
    }
}

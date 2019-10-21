package antrpc.commons.config;

public class MetricsConfig {

    private boolean enableConsoleReporter = false;
    private boolean enableLoggerReporter = false;
    private boolean enableJmxReporter = false;
    private boolean enableAntrpcMonitorReporter = false;
    private boolean enableServlets = false;
    private long reportIntervalSeconds;
    private boolean enable = false;

    public boolean isEnableAntrpcMonitorReporter() {
        return enableAntrpcMonitorReporter;
    }

    public void setEnableAntrpcMonitorReporter(boolean enableAntrpcMonitorReporter) {
        this.enableAntrpcMonitorReporter = enableAntrpcMonitorReporter;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnableServlets() {
        return enableServlets;
    }

    public void setEnableServlets(boolean enableServlets) {
        this.enableServlets = enableServlets;
    }

    public boolean isEnableJmxReporter() {
        return enableJmxReporter;
    }

    public void setEnableJmxReporter(boolean enableJmxReporter) {
        this.enableJmxReporter = enableJmxReporter;
    }

    public boolean isEnableConsoleReporter() {
        return enableConsoleReporter;
    }

    public void setEnableConsoleReporter(boolean enableConsoleReporter) {
        this.enableConsoleReporter = enableConsoleReporter;
    }

    public boolean isEnableLoggerReporter() {
        return enableLoggerReporter;
    }

    public void setEnableLoggerReporter(boolean enableLoggerReporter) {
        this.enableLoggerReporter = enableLoggerReporter;
    }

    public long getReportIntervalSeconds() {
        return reportIntervalSeconds;
    }

    public void setReportIntervalSeconds(long reportIntervalSeconds) {
        this.reportIntervalSeconds = reportIntervalSeconds;
    }
}

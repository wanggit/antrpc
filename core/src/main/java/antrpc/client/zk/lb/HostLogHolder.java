package antrpc.client.zk.lb;

import antrpc.commons.test.ForTest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ForTest
@Slf4j
class HostLogHolder {

    private ConcurrentHashMap<String, AtomicLong> statsMap = new ConcurrentHashMap<>();

    private HostLogHolder() {}

    private static final HostLogHolder holder = new HostLogHolder();

    public static HostLogHolder getInstance() {
        return holder;
    }

    @ForTest
    void log(String hostInfo) {
        if (!statsMap.containsKey(hostInfo)) {
            statsMap.put(hostInfo, new AtomicLong(0));
        }
        statsMap.get(hostInfo).incrementAndGet();
    }

    @ForTest
    Map<String, Long> snapshot() {
        Map<String, Long> data = new HashMap<>((int) (statsMap.size() / 0.75f));
        statsMap.forEach(
                (key, value) -> {
                    data.put(key, value.longValue());
                });
        return data;
    }
}

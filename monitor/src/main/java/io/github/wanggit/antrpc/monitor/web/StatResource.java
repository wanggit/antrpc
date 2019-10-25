package io.github.wanggit.antrpc.monitor.web;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.monitor.domain.Jvm;
import io.github.wanggit.antrpc.monitor.service.CallLogsService;
import io.github.wanggit.antrpc.monitor.service.JvmService;
import io.github.wanggit.antrpc.monitor.service.dto.LastHourStatDTO;
import io.github.wanggit.antrpc.monitor.web.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/stat")
public class StatResource {

    private static final String STAT_SUMMARY_CACHE_KEY = "stat_summary";

    private static final String STAT_APPS_CACHE_KEY = "stat_apps";

    private static final String STAT_IP_CACHE_KEY = "stat_ip";

    @Autowired private IAntrpcContext antrpcContext;

    @Autowired private CallLogsService callLogsService;

    @Autowired private JvmService jvmService;

    private final LoadingCache<String, Result> loadingCache =
            CacheBuilder.newBuilder()
                    .refreshAfterWrite(1, TimeUnit.SECONDS)
                    .maximumSize(100)
                    .build(
                            new CacheLoader<String, Result>() {
                                @Override
                                public Result load(String key) throws Exception {
                                    if (STAT_SUMMARY_CACHE_KEY.equals(key)) {
                                        return internalStatSummary();
                                    } else if (STAT_APPS_CACHE_KEY.equals(key)) {
                                        return internalStatApps();
                                    } else if (STAT_IP_CACHE_KEY.equals(key)) {
                                        return internalStatIps();
                                    }
                                    return null;
                                }
                            });

    private ExecutorService executorService =
            new ThreadPoolExecutor(
                    10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(500));

    @GetMapping("/testSave")
    public String testSave()
            throws InterruptedException, ExecutionException, TimeoutException, IOException {
        String[] apps = new String[1000];
        for (int i = 0; i < apps.length; i++) {
            apps[i] = "App" + RandomStringUtils.randomAlphanumeric(20);
        }
        String[] attrNames = new String[500];
        for (int i = 0; i < attrNames.length; i++) {
            attrNames[i] = "AttrName" + RandomStringUtils.randomAlphanumeric(20);
        }
        int times = 100;
        StopWatch watch = new StopWatch();
        watch.start("save");
        List<Future<Integer>> futures = new ArrayList<>(times * 2);
        for (int i = 0; i < times; i++) {
            Jvm jvm = new Jvm();
            jvm.setId(UUID.randomUUID().toString());
            jvm.setAppName(apps[RandomUtils.nextInt(0, apps.length)]);
            jvm.setAttrName(attrNames[RandomUtils.nextInt(0, attrNames.length)]);
            jvm.setAttrValue(String.valueOf(RandomUtils.nextLong(0, Long.MAX_VALUE)));
            jvm.setTs(System.currentTimeMillis());
            jvmService.save(jvm);

            /*try {
                Future<Integer> future =
                        executorService.submit(
                                new Callable<Integer>() {
                                    @Override
                                    public Integer call() {
                                        Jvm jvm = new Jvm();
                                        jvm.setAppName(apps[RandomUtils.nextInt(0, apps.length)]);
                                        jvm.setAttrName(
                                                attrNames[
                                                        RandomUtils.nextInt(0, attrNames.length)]);
                                        jvm.setAttrValue(
                                                String.valueOf(
                                                        RandomUtils.nextLong(0, Long.MAX_VALUE)));
                                        jvm.setTs(System.currentTimeMillis());
                                        jvmService.save(jvm);
                                        return 0;
                                    }
                                });
                futures.add(future);
            } catch (RejectedExecutionException e) {
                System.out.println("full , sleep 200ms");
                Thread.sleep(200);
            }*/
        }
        /*for (Future<Integer> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }*/
        watch.stop();
        System.out.println(watch.prettyPrint());
        return "OK";
    }

    /**
     * 查询jvm heap
     *
     * @return
     */
    @GetMapping("/stat_jvmheap")
    public Result<LineChartVO<Double>> statJvmHeap(
            @RequestParam("appName") String appName,
            @RequestParam(value = "start", required = false) Long start,
            @RequestParam(value = "end", required = false) Long end) {
        if (null == start || null == end) {
            DateTime standard = new DateTime();
            end = standard.toDate().getTime();
            start = standard.minusDays(7).toDate().getTime();
        }
        LineChartVO<Double> lineChartVO = jvmService.statJvmHeap(appName, start, end);
        return new Result<>(lineChartVO);
    }

    /**
     * 查询各台服务器上服务的分布
     *
     * @return
     */
    @GetMapping("/stat_ips")
    public Result<PieChartVO<Integer>> statIps() {
        try {
            return loadingCache.get(STAT_IP_CACHE_KEY);
        } catch (ExecutionException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return new Result<>(ResultCode.ERROR);
        }
    }

    private Result<PieChartVO<Integer>> internalStatIps() {
        try {
            List<String> ipPaths =
                    antrpcContext
                            .getZkClient()
                            .getCurator()
                            .getChildren()
                            .forPath("/" + ConstantValues.ZK_ROOT_NODE_NAME);
            PieChartVO<Integer> pieChartVO = new PieChartVO<>();
            pieChartVO.setTitle("服务器分布");
            Map<String, AtomicInteger> ipMap = new HashMap<>();
            for (String ipPath : ipPaths) {
                byte[] bytes =
                        antrpcContext
                                .getZkClient()
                                .getCurator()
                                .getData()
                                .forPath("/" + ConstantValues.ZK_ROOT_NODE_NAME + "/" + ipPath);
                RegisterBean.IpNodeDataBean ipNodeDataBean =
                        JSONObject.parseObject(
                                new String(bytes, Charset.forName("UTF-8")),
                                RegisterBean.IpNodeDataBean.class);
                String appName = ipNodeDataBean.getAppName();
                String[] tmps = appName.split("@");
                String ip = tmps[1].substring(0, tmps[1].lastIndexOf(":"));
                if (!ipMap.containsKey(ip)) {
                    ipMap.put(ip, new AtomicInteger(0));
                }
                ipMap.get(ip).incrementAndGet();
            }

            List<PieChartVO.SeriesData<Integer>> seriesDatas = new ArrayList<>();
            ipMap.forEach(
                    (key, value) -> {
                        PieChartVO.SeriesData<Integer> seriesData = new PieChartVO.SeriesData<>();
                        seriesData.setName(key);
                        seriesData.setValue(value.intValue());
                        seriesDatas.add(seriesData);
                    });
            pieChartVO.setSeries(seriesDatas);
            return new Result<>(pieChartVO);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("stat ip error. ", e);
            }
        }
        return null;
    }

    /**
     * 查询各个应用数量
     *
     * @return
     */
    @GetMapping("/stat_apps")
    public Result<PieChartVO<Integer>> statApps() {
        try {
            return loadingCache.get(STAT_APPS_CACHE_KEY);
        } catch (ExecutionException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return new Result<>(ResultCode.ERROR);
        }
    }

    private Result<PieChartVO<Integer>> internalStatApps() {
        try {
            List<String> ipPaths =
                    antrpcContext
                            .getZkClient()
                            .getCurator()
                            .getChildren()
                            .forPath("/" + ConstantValues.ZK_ROOT_NODE_NAME);
            PieChartVO<Integer> pieChartVO = new PieChartVO<>();
            pieChartVO.setTitle("应用分布");
            Map<String, AtomicInteger> appNameMap = new HashMap<>();
            for (String ipPath : ipPaths) {
                byte[] bytes =
                        antrpcContext
                                .getZkClient()
                                .getCurator()
                                .getData()
                                .forPath("/" + ConstantValues.ZK_ROOT_NODE_NAME + "/" + ipPath);
                RegisterBean.IpNodeDataBean ipNodeDataBean =
                        JSONObject.parseObject(
                                new String(bytes, Charset.forName("UTF-8")),
                                RegisterBean.IpNodeDataBean.class);
                String appName = ipNodeDataBean.getAppName();
                String[] tmps = appName.split("@");
                String name = tmps[0];
                if (!appNameMap.containsKey(name)) {
                    appNameMap.put(name, new AtomicInteger(0));
                }
                appNameMap.get(name).incrementAndGet();
            }

            List<PieChartVO.SeriesData<Integer>> seriesDatas = new ArrayList<>();
            appNameMap.forEach(
                    (key, value) -> {
                        PieChartVO.SeriesData<Integer> seriesData = new PieChartVO.SeriesData<>();
                        seriesData.setName(key);
                        seriesData.setValue(value.intValue());
                        seriesDatas.add(seriesData);
                    });
            pieChartVO.setSeries(seriesDatas);
            return new Result<>(pieChartVO);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("stat app error. ", e);
            }
        }
        return null;
    }

    /**
     * 查询概要统计
     *
     * @return
     */
    @GetMapping("/stat_summary")
    public Result<StatVO> statSummary() {
        try {
            return loadingCache.get(STAT_SUMMARY_CACHE_KEY);
        } catch (ExecutionException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return new Result<>(ResultCode.ERROR);
        }
    }

    private Result<StatVO> internalStatSummary() {
        try {
            StatVO statVO = new StatVO();
            List<String> subPaths =
                    antrpcContext
                            .getZkClient()
                            .getCurator()
                            .getChildren()
                            .forPath("/" + ConstantValues.ZK_ROOT_NODE_NAME);
            statVO.setNodeCount(subPaths.size());
            statVO.setInterfaceCount(antrpcContext.getNodeHostContainer().snapshot().size());
            LastHourStatDTO lastHourStatDTO = callLogsService.rpcCallLogLastHour();
            statVO.setCallInLastHour(lastHourStatDTO.getCallCount());
            statVO.setAvgRt(lastHourStatDTO.getAvgRt());
            return new Result<>(statVO);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("query summary error.", e);
            }
            return new Result<>(ResultCode.ERROR);
        }
    }
}

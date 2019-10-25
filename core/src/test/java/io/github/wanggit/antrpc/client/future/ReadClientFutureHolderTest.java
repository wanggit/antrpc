package io.github.wanggit.antrpc.client.future;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.bean.RpcResponseBean;
import io.github.wanggit.antrpc.commons.codec.kryo.KryoSerializer;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReadClientFutureHolderTest {

    @Test
    public void testReadClintFutrueHolder() throws Exception {
        ReadClientFuture future = ReadClientFutureHolder.createFuture(1);
        RpcResponseBean rpcResponseBean = new RpcResponseBean();
        rpcResponseBean.setResult(RandomStringUtils.randomAlphanumeric(10));
        rpcResponseBean.setId(RandomStringUtils.randomAlphanumeric(10));
        rpcResponseBean.setReqTs(System.currentTimeMillis());
        rpcResponseBean.setTs(System.currentTimeMillis());
        RpcProtocol rpcProtocol = new RpcProtocol();
        rpcProtocol.setCmdId(1);
        rpcProtocol.setType(ConstantValues.BIZ_TYPE);
        rpcProtocol.setData(KryoSerializer.getInstance().serialize(rpcResponseBean));
        ReadClientFutureHolder.receive(rpcProtocol);
        RpcResponseBean resultEntity = future.get();
        Assert.assertEquals(resultEntity.getId(), rpcResponseBean.getId());
    }

    @Test
    public void testManyReadClintFutrueHolder() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        List<Future<ReadClientFuture>> callFutures = new ArrayList<>(1000);
        for (int i = 0; i < 1000; i++) {
            int idx = i;
            Future<ReadClientFuture> submit =
                    executorService.submit(() -> ReadClientFutureHolder.createFuture(idx));
            callFutures.add(submit);
        }

        for (int i = 0; i < 1000; i++) {
            RpcResponseBean rpcResponseBean = new RpcResponseBean();
            rpcResponseBean.setResult("result" + i);
            rpcResponseBean.setId("id" + i);
            rpcResponseBean.setReqTs(System.currentTimeMillis());
            rpcResponseBean.setTs(System.currentTimeMillis());
            RpcProtocol rpcProtocol = new RpcProtocol();
            rpcProtocol.setCmdId(i);
            rpcProtocol.setType(ConstantValues.BIZ_TYPE);
            rpcProtocol.setData(KryoSerializer.getInstance().serialize(rpcResponseBean));
            ReadClientFutureHolder.receive(rpcProtocol);
        }

        int count = 0;
        for (Future<ReadClientFuture> callFuture : callFutures) {
            ReadClientFuture readClientFuture = callFuture.get();
            RpcResponseBean rpcResponseBean = readClientFuture.get();
            Assert.assertNotNull(rpcResponseBean);
            Assert.assertTrue(rpcResponseBean.getResult().toString().contains("result"));
            Assert.assertTrue(rpcResponseBean.getId().contains("id"));
            Assert.assertEquals(
                    rpcResponseBean.getId().replaceFirst("id", ""),
                    rpcResponseBean.getResult().toString().replaceFirst("result", ""));
            System.out.println(rpcResponseBean);
            count++;
        }

        WaitUtil.wait(5, 1);
        Assert.assertEquals(1000, count);
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme

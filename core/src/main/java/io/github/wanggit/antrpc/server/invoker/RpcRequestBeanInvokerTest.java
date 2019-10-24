package io.github.wanggit.antrpc.server.invoker;

import com.google.common.collect.Lists;
import io.github.wanggit.antrpc.commons.bean.IdGenHelper;
import io.github.wanggit.antrpc.commons.bean.RpcRequestBean;
import io.github.wanggit.antrpc.commons.bean.RpcResponseBean;
import io.github.wanggit.antrpc.commons.bean.SerialNumberThreadLocal;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class RpcRequestBeanInvokerTest {

    @Test
    public void invoke() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton(AInterface.class.getName(), new AImpl());
        RpcRequestBeanInvoker invoker = new RpcRequestBeanInvoker(beanFactory);
        RpcRequestBean requestBean = new RpcRequestBean();
        requestBean.setTs(System.currentTimeMillis());
        SerialNumberThreadLocal.TraceEntity traceEntity = SerialNumberThreadLocal.get();
        requestBean.setSerialNumber(traceEntity.getSerialNumber());
        requestBean.setCaller(traceEntity.getCaller());
        requestBean.setOneway(false);
        requestBean.setArgumentValues(new Object[] {});
        requestBean.setMethodName("getName");
        requestBean.setArgumentTypes(Lists.newArrayList());
        requestBean.setFullClassName(AInterface.class.getName());
        requestBean.setId(IdGenHelper.getInstance().getUUID());
        RpcResponseBean responseBean = invoker.invoke(requestBean);
        Assert.assertNotNull(responseBean);
        Assert.assertNotNull(responseBean.getResult());
        Assert.assertTrue(responseBean.getResult() instanceof String);
        String result = responseBean.getResult().toString();
        System.out.println(result);
        Assert.assertEquals(responseBean.getReqTs(), requestBean.getTs());
        Assert.assertEquals(responseBean.getId(), requestBean.getId());
    }

    interface AInterface {
        String getName();
    }

    public static class AImpl implements AInterface {

        @Override
        public String getName() {
            return "Name: " + RandomStringUtils.randomAlphanumeric(10);
        }
    }
}

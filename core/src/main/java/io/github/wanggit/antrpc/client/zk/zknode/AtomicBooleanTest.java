package io.github.wanggit.antrpc.client.zk.zknode;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class AtomicBooleanTest {

    @Test
    public void testAtomicBoolean(){
        final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        for (int i = 0; i < 100; i++) {
            if (i ==0){
                Assert.assertTrue(atomicBoolean.compareAndSet(false, true));
            }else {
                Assert.assertFalse(atomicBoolean.compareAndSet(false, true));
            }
        }
    }

}

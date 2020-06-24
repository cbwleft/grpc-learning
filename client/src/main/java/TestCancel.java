import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Copyright (c) 2020 XiaoMi Inc.All Rights Reserved.
 * Description:
 *
 * @author cuibowen@xiaomi.com
 * Date:2020/6/24 上午10:04
 */
public class TestCancel {

    public static void main(String[] args) {
        HelloWorldClient helloWorldClient = new HelloWorldClient("localhost", 50052);
        ListenableFuture<Hello.HelloReply> testCancel = helloWorldClient.testCancelPropagation("testCancel");
        try {
            Hello.HelloReply helloReply = testCancel.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        testCancel.cancel(true);
        System.out.println("canceled");
    }
}

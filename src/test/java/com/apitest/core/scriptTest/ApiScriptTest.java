package com.apitest.core.scriptTest;

import com.apitest.annotations.TestData;
import com.apitest.core.ApiScript;
import com.apitest.core.lifeCycleTest.ApiBaseDataLifeCycleTest;
import com.apitest.core.lifeCycleTest.LifeCycleTestData;
import com.apitest.dataProvider.Self;
import com.apitest.dataProvider.Spring;
import com.apitest.testModels.Console;
import kotlin.Pair;
import org.springframework.util.ReflectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@TestData
public class ApiScriptTest {

    @Test
    public void baseScriptTest(){
        Method m = ReflectionUtils.findMethod(ApiBaseDataLifeCycleTest.class,"test", LifeCycleTestData.class);
        ApiScript apiScript = new ApiScript(m);
        apiScript.execute();
    }

    @Test
    public void flowTest_001(TestFlowData data123){
        Assert.assertNotNull(data123.getName());
        Assert.assertNotEquals(data123.getName(),"@name");
    }

    @Test
    public void flowTest_002(TestFlowData data){
        Assert.assertEquals(data.getName(),"18");
    }

    @Test
    public void fT_003() throws NoSuchMethodException {
       Method method = ApiScriptTest.class.getDeclaredMethod("flowTest_001", TestFlowData.class);
        Parameter p1 = method.getParameters()[0];
    }





}

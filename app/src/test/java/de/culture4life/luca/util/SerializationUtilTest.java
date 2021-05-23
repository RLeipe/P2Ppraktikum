package de.culture4life.luca.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashMap;

import androidx.test.runner.AndroidJUnit4;

@Config(sdk = 28)
@RunWith(AndroidJUnit4.class)
public class SerializationUtilTest {

    private String TESTSTRING = "abcde1234!§$% äöü @ ,.-;:_*<>";

    @Test
    public void serializeToAndFromBase32() {
        SerializationUtil.serializeBase32(TESTSTRING.getBytes())
                .map(serialized -> SerializationUtil.deserializeFromBase32(serialized))
                .test()
                .assertValue(value -> Arrays.equals(value.blockingGet(), TESTSTRING.getBytes()));
    }

    @Test
    public void serializeToAndFromBase64() {
        SerializationUtil.serializeToBase64(TESTSTRING.getBytes())
                .map(serialized -> SerializationUtil.deserializeFromBase64(serialized))
                .test()
                .assertValue(value -> Arrays.equals(value.blockingGet(), TESTSTRING.getBytes()));
    }

    @Test
    public void serializeToAndFromJson() {
        HashMap testObject = new HashMap();
        testObject.put("testString", TESTSTRING);
        SerializationUtil.serializeToJson(testObject)
                .map(serialized -> SerializationUtil.deserializeFromJson(serialized, HashMap.class))
                .test()
                .assertValue(value -> value.blockingGet().equals(testObject));
    }

}
package ske.registry.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import ske.mag.test.kategorier.Enhetstest;

@Category(Enhetstest.class)
public class JsonHelperTest {

    @Test
    public void skalLageJsonAvMapEnEntry() throws Exception {
        assertThat(JsonHelper.tilJson(ImmutableMap.of("k1", "v1")), is("{\"k1\":\"v1\"}"));
    }

    @Test
    public void skalLageJsonAvMapAvToEntries() throws Exception {
        assertThat(JsonHelper.tilJson(ImmutableMap.of("k1", "v1", "k2", "v2")), is("{\"k1\":\"v1\", \"k2\":\"v2\"}"));
    }

}

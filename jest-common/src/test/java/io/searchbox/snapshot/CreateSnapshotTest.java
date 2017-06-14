package io.searchbox.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author happyprg(hongsgo@gmail.com)
 */
public class CreateSnapshotTest {

    private String repository = "leeseohoo";
    private String snapshot = "leeseola";

    @Test
    public void testSnapshot() {
        CreateSnapshot createSnapshot = new CreateSnapshot.Builder(repository, snapshot).waitForCompletion(true).build();
        assertEquals("PUT", createSnapshot.getRestMethodName());
        assertEquals("/_snapshot/leeseohoo/leeseola?wait_for_completion=true", createSnapshot.getURI());
    }

    @Test
    public void testSnapshotWithSettings() throws IOException {

        final Settings.Builder registerRepositorySettings = Settings.settingsBuilder();
        registerRepositorySettings.put("indices", "index_1,index_2");
        registerRepositorySettings.put("ignore_unavailable", "true");
        registerRepositorySettings.put("include_global_state", "false");

        CreateSnapshot createSnapshot = new CreateSnapshot.Builder(repository, snapshot)
                .settings(registerRepositorySettings.build().getAsMap())
                .waitForCompletion(true)
                .build();

        assertEquals("PUT", createSnapshot.getRestMethodName());
        assertEquals("/_snapshot/leeseohoo/leeseola?wait_for_completion=true", createSnapshot.getURI());
        final ObjectMapper objectMapper = new ObjectMapper();
        String settings = objectMapper.writeValueAsString(createSnapshot.getData(objectMapper));
        assertEquals("\"{\\\"ignore_unavailable\\\":\\\"true\\\",\\\"include_global_state\\\":\\\"false\\\",\\\"indices\\\":\\\"index_1,index_2\\\"}\"", settings);
    }
}

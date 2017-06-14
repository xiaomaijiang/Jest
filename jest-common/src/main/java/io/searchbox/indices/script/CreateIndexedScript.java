package io.searchbox.indices.script;

import com.google.common.io.CharStreams;
import io.searchbox.action.AbstractAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

public class CreateIndexedScript extends AbstractIndexedScript {

    protected CreateIndexedScript(Builder builder) {
        super(builder);
        this.payload = builder.payload;
        setURI(buildURI());
    }

    @Override
    public String getRestMethodName() {
        return "POST";
    }

    public String getScriptName() {
        return scriptName;
    }

    public ScriptLanguage getScriptLanguage() {
        return scriptLanguage;
    }

    public static class Builder extends AbstractIndexedScript.Builder<CreateIndexedScript, Builder> {

        private Map<String, String> payload;

        public Builder(String scriptName) {
            super(scriptName);
        }

        @Override
        public CreateIndexedScript build() {
            return new CreateIndexedScript(this);
        }

        public Builder setSource(String source) {
            createPayload(source);
            return this;
        }

        public Builder loadSource(File srcFile) throws IOException {
            return loadSource(srcFile, Charset.forName(AbstractAction.CHARSET));
        }

        public Builder loadSource(File srcFile, Charset encoding) throws IOException {
            return loadSource(new FileInputStream(srcFile), encoding);
        }

        public Builder loadSource(InputStream srcStream) throws IOException {
            return loadSource(srcStream, Charset.forName(AbstractAction.CHARSET));
        }

        public Builder loadSource(InputStream srcStream, Charset encoding) throws IOException {
            String src = CharStreams.toString(new InputStreamReader(srcStream, encoding));
            createPayload(src);
            return this;
        }

        private void createPayload(String source) {
            payload = Collections.singletonMap("script", source);
        }

    }
}

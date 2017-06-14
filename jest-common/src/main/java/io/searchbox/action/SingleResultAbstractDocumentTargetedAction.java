package io.searchbox.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.searchbox.core.DocumentResult;

import java.io.IOException;

/**
 * @author Bartosz Polnik
 */
public abstract class SingleResultAbstractDocumentTargetedAction extends AbstractDocumentTargetedAction<DocumentResult> {
    public SingleResultAbstractDocumentTargetedAction(Builder builder) {
        super(builder);
    }

    @Override
    public DocumentResult createNewElasticSearchResult(String responseBody, int statusCode, String reasonPhrase, ObjectMapper objectMapper) throws IOException {
        return createNewElasticSearchResult(new DocumentResult(objectMapper), responseBody, statusCode, reasonPhrase, objectMapper);
    }
}

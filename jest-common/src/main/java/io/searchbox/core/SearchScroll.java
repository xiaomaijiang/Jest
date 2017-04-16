package io.searchbox.core;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import io.searchbox.action.AbstractMultiIndexActionBuilder;
import io.searchbox.action.GenericResultAbstractAction;
import io.searchbox.params.Parameters;

/**
 * @author ferhat
 */
public class SearchScroll extends GenericResultAbstractAction {
    @VisibleForTesting
    static final int MAX_SCROLL_ID_LENGTH = 1900;
    private final String restMethodName;


    protected SearchScroll(Builder builder) {
        super(builder);

        if(builder.getScrollId().length() > MAX_SCROLL_ID_LENGTH) {
            this.restMethodName = "POST";
            // represent scroll_id in json for request body
            this.payload = "{\"scroll_id\":\"" + builder.getScrollId() + "\"}";
        } else {
            this.restMethodName = "GET";
        }
        setURI(buildURI());
    }

    @Override
    protected String buildURI() {
        return super.buildURI() + "/_search/scroll";
    }

    @Override
    public String getRestMethodName() {
        return this.restMethodName;
    }

    @Override
    public String getPathToResult() {
        return "hits/hits/_source";
    }

    public static class Builder extends AbstractMultiIndexActionBuilder<SearchScroll, Builder> {

        private final String scrollId;

        public Builder(String scrollId, String scroll) {
            this.scrollId = scrollId;
            if (scrollId.length() <= MAX_SCROLL_ID_LENGTH) {
                setParameter(Parameters.SCROLL_ID, scrollId);
            }
            setParameter(Parameters.SCROLL, scroll);
        }

        @Override
        public String getJoinedIndices() {
            if (indexNames.size() > 0) {
                return Joiner.on(',').join(indexNames);
            } else {
                return null;
            }
        }

        @Override
        public SearchScroll build() {
            return new SearchScroll(this);
        }

        public String getScrollId() {
            return scrollId;
        }
    }
}

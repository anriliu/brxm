package org.onehippo.cms7.crisp.core.resource;

import org.onehippo.cms7.crisp.api.resource.ResourceLink;

public class SimpleResourceLink implements ResourceLink {

    private String uri;

    public SimpleResourceLink(String uri) {
        this.uri = uri;
    }

    @Override
    public String getUri() {
        return uri;
    }
}

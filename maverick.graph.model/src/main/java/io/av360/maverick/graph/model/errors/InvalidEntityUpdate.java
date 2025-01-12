package io.av360.maverick.graph.model.errors;

import org.eclipse.rdf4j.model.Resource;
import org.springframework.util.StringUtils;

public class InvalidEntityUpdate extends Exception {
    private Resource identifier;
    private String reason;

    public InvalidEntityUpdate(Resource entityIdentifier, String reason) {
        this.identifier = entityIdentifier;
        this.reason = reason;
    }


    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Invalid update for entity with id '").append(identifier).append("'.");
        if (StringUtils.hasLength(this.reason)) {
            sb.append(this.reason).append(".");
        }
        return sb.toString();
    }
}

package io.av360.maverick.graph.feature.applications.domain.events;

import io.av360.maverick.graph.feature.applications.domain.model.Application;

public class ApplicationCreatedEvent {
    private final Application app;

    public ApplicationCreatedEvent(Application app) {

        this.app = app;
    }
}

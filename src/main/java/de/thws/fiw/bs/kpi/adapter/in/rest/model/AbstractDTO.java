package de.thws.fiw.bs.kpi.adapter.in.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public abstract class AbstractDTO {

    protected UUID id;

    @JsonProperty("_self")
    protected SelfLink selfLink;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public SelfLink getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(SelfLink selfLink) {
        this.selfLink = selfLink;
    }
}

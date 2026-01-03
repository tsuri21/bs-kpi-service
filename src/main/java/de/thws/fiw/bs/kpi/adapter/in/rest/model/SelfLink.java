package de.thws.fiw.bs.kpi.adapter.in.rest.model;

public class SelfLink {
    private String href;
    private String rel;
    private String type;
    private String method;

    public SelfLink() {}

    public SelfLink(String href, String rel, String type, String method) {
        this.href = href;
        this.rel = rel;
        this.type = type;
        this.method = method;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}

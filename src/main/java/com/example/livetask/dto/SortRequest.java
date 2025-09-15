package com.example.livetask.dto;

import java.util.List;

public class SortRequest {
    private List<String> tags;
    private Boolean desc;
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public Boolean getDesc() { return desc; }
    public void setDesc(Boolean desc) { this.desc = desc; }
}

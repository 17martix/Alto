package com.structurecode.alto.Models;

public class SearchConfig {
    private String admin_key;
    private String application_id;
    private String search_key;

    public SearchConfig() {
    }

    public SearchConfig(String admin_key, String application_id, String search_key) {
        this.admin_key = admin_key;
        this.application_id = application_id;
        this.search_key = search_key;
    }

    public String getAdmin_key() {
        return admin_key;
    }

    public void setAdmin_key(String admin_key) {
        this.admin_key = admin_key;
    }

    public String getApplication_id() {
        return application_id;
    }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
    }

    public String getSearch_key() {
        return search_key;
    }

    public void setSearch_key(String search_key) {
        this.search_key = search_key;
    }
}

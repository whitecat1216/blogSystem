package com.example.app.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ScreenDefinition {
    private String title;
    private String tableName;
    private List<SearchField> searchFields;
    private List<ListColumn> listColumns;
    private List<FormField> formFields;
    private Pagination pagination;
    private ListLayout listLayout;
    private DetailLayout detailLayout;

    @Data
    public static class SearchField {
        private String key;
        private String label;
        private String type;
        private String placeholder;
        private List<String> options;
    }

    @Data
    public static class ListColumn {
        private String key;
        private String label;
        private Boolean sortable;
    }

    @Data
    public static class FormField {
        private String key;
        private String label;
        private String type;
        private Boolean required;
        private List<String> options;
        private String accept;      // for file/image input e.g. "image/*"
        private Integer maxSizeMb;  // max upload size
        private String uploadEndpoint; // API endpoint for uploading
        // for multiselect dynamic options
        private String source; // e.g. "/api/tag/list"
        private Boolean allowCreate; // allow creating new option via POST
    }

    @Data
    public static class Pagination {
        private Integer pageSize;
    }

    @Data
    public static class ListLayout {
        private String type; // e.g., "card"
        private String titleField; // e.g., "title"
        private String dateField;  // e.g., "created_at"
        private String excerptField; // e.g., "excerpt"
        private String imageField;   // e.g., "hero_image"
        private String tagsField;    // e.g., "tags"
    }

    @Data
    public static class DetailLayout {
        private String titleField;
        private String dateField;
        private String imageField;
        private String tagsField;
        private String excerptField;
        private String contentField;
        private List<String> order;
    }
}

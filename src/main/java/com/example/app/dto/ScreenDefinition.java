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
    }

    @Data
    public static class Pagination {
        private Integer pageSize;
    }
}

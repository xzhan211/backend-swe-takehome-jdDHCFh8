package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PaginatedResponse<T> {
    
    private List<T> content;
    private PageInfo page;
    
    public PaginatedResponse() {}
    
    public PaginatedResponse(List<T> content, PageInfo page) {
        this.content = content;
        this.page = page;
    }
    
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public PageInfo getPage() {
        return page;
    }
    
    public void setPage(PageInfo page) {
        this.page = page;
    }
    
    public static class PageInfo {
        private int number;
        private int size;
        @JsonProperty("totalElements")
        private long totalElements;
        @JsonProperty("totalPages")
        private int totalPages;
        private boolean first;
        private boolean last;
        
        public PageInfo() {}
        
        public PageInfo(int number, int size, long totalElements, int totalPages, boolean first, boolean last) {
            this.number = number;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
        }
        
        // Getters and Setters
        public int getNumber() {
            return number;
        }
        
        public void setNumber(int number) {
            this.number = number;
        }
        
        public int getSize() {
            return size;
        }
        
        public void setSize(int size) {
            this.size = size;
        }
        
        public long getTotalElements() {
            return totalElements;
        }
        
        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }
        
        public int getTotalPages() {
            return totalPages;
        }
        
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
        
        public boolean isFirst() {
            return first;
        }
        
        public void setFirst(boolean first) {
            this.first = first;
        }
        
        public boolean isLast() {
            return last;
        }
        
        public void setLast(boolean last) {
            this.last = last;
        }
    }
}

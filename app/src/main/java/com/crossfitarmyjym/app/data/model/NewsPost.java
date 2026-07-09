package com.crossfitarmyjym.app.data.model;

import com.google.gson.annotations.SerializedName;

public class NewsPost {

    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_PUBLISHED = "published";
    public static final String STATUS_ARCHIVED = "archived";

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("summary")
    private String summary;

    @SerializedName("body")
    private String body;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("status")
    private String status;

    @SerializedName("published_at")
    private String publishedAt;

    @SerializedName("created_by")
    private String createdBy;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getBody() {
        return body;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public boolean isPublished() {
        return STATUS_PUBLISHED.equals(status);
    }
}

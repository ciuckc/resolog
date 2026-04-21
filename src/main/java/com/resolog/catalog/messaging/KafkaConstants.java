package com.resolog.catalog.messaging;

public class KafkaConstants {
    public static final String PRODUCT_CREATED = "product-created";
    public static final String PRODUCT_SUBMITTED_FOR_PUBLISHING = "product-submitted-for-publishing";
    public static final String PRODUCT_PUBLISHED = "product-published";
    public static final String PRODUCT_REJECTED = "product-rejected";
    public static final String PRODUCT_UNPUBLISHED = "product-unpublished";
    public static final String PRODUCT_DELETED = "product-deleted";
    public static final String PRODUCT_SUBMISSION_APPROVED = "product-submission-approved";
    public static final String PRODUCT_SUBMISSION_DECLINED = "product-submission-declined";

    public static final String HEADER_MESSAGE_ID = "message-id";

    public static final String GROUP_CATALOG_SERVICE = "catalog-service";
    public static final String GROUP_MODERATION_SERVICE = "moderation-service";
    public static final String GROUP_ARCHIVAL_SERVICE = "archival-service";
    public static final String GROUP_DSP_SERVICE = "dsp-service";
    public static final String GROUP_DASHBOARD_SERVICE = "dashboard-service";
    public static final String GROUP_NOTIFICATION_SERVICE = "notification-service";
}

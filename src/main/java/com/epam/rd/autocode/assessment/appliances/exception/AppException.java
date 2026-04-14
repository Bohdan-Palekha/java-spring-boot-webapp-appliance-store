package com.epam.rd.autocode.assessment.appliances.exception;

public abstract class AppException extends RuntimeException {
    private final String messageKey;
    private final Long resourceId;

    protected AppException(String messageKey, Long resourceId) {
        super(messageKey + (resourceId != null ? " [id=" + resourceId + "]" : ""));
        this.messageKey = messageKey;
        this.resourceId = resourceId;
    }

    protected AppException(String messageKey) {
        this(messageKey, null);
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Long getResourceId() {
        return resourceId;
    }
}

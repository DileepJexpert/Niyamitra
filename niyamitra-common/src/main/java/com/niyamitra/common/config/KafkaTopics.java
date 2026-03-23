package com.niyamitra.common.config;

public final class KafkaTopics {

    private KafkaTopics() {}

    // Niyamitra core platform events
    public static final String DOCUMENT_UPLOADED = "niyamitra.document-uploaded";
    public static final String DOCUMENT_EXTRACTED = "niyamitra.document-extracted";
    public static final String TENANT_ONBOARDED = "niyamitra.tenant-onboarded";

    // Anupalan compliance intelligence events
    public static final String TASKS_GENERATED = "anupalan.tasks-generated";
    public static final String GAZETTE_FOUND = "anupalan.gazette-found";

    // Kavach AI agent events
    public static final String WHATSAPP_RECEIVED = "kavach.whatsapp-received";
    public static final String WHATSAPP_SEND = "kavach.whatsapp-send";
    public static final String EXPIRY_APPROACHING = "kavach.expiry-approaching";
    public static final String ESCALATION_TRIGGERED = "kavach.escalation-triggered";
    public static final String PORTAL_CHECK_REQUESTED = "kavach.portal-check-requested";
    public static final String PORTAL_CHECK_COMPLETED = "kavach.portal-check-completed";
}

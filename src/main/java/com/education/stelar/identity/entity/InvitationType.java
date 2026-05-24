package com.education.stelar.identity.entity;

public enum InvitationType {
    /** User does not exist in the system — an email is sent with a registration link. */
    EXTERNAL,
    /** User already exists in another tenant — no email is sent. */
    INTERNAL
}

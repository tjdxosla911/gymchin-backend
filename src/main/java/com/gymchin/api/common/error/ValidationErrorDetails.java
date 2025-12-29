package com.gymchin.api.common.error;

import java.util.List;

public class ValidationErrorDetails {
    private final List<FieldErrorDetail> fieldErrors;

    public ValidationErrorDetails(List<FieldErrorDetail> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public List<FieldErrorDetail> getFieldErrors() {
        return fieldErrors;
    }
}

package de.innologic.iamservice.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of subject (user or service principal)", allowableValues = {"USER", "SERVICE"}, example = "USER")
public enum SubjectType {
    USER,
    SERVICE
}

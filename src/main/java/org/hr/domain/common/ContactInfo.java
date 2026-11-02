package org.hr.domain.common;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;

/** Value Object (custom data type): date de contact ale unei persoane. */
@Value
public class ContactInfo implements Serializable {
    @NotBlank(message = "Email is required!")
    @Email(message = "Email must be a valid address!")
    @Size(max = 150, message = "Email is too long!")
    String email;

    @Pattern(regexp = "^$|^\\+?[0-9 .\\-()]{6,20}$", message = "Phone number has an invalid format!")
    String phone;

    public static ContactInfo of(String email, String phone) {
        return new ContactInfo(email, phone);
    }
}

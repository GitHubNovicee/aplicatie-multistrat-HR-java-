package org.hr.domain.common;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;

/** Value Object (custom data type): date de contact ale unei persoane. */
@Embeddable
@Value
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class ContactInfo implements Serializable {
    @NotBlank(message = "Email is required!")
    @Email(message = "Email must be a valid address!")
    @Size(max = 150, message = "Email is too long!")
    @jakarta.persistence.Column(name = "email", nullable = false, length = 150)
    String email;

    @Pattern(regexp = "^$|^\\+?[0-9 .\\-()]{6,20}$", message = "Phone number has an invalid format!")
    @jakarta.persistence.Column(name = "phone", length = 30)
    String phone;

    public static ContactInfo of(String email, String phone) {
        return new ContactInfo(email, phone);
    }
}

package org.hr.domain.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

/** RQ-02: ierarhie de departamente cu sef de departament. */
@Getter @Setter @NoArgsConstructor
public class Department extends BaseEntity {
    @NotBlank(message = "Department name is required!")
    @Size(max = 100, message = "Department name is too long!")
    private String name;
    @Size(max = 500)
    private String description;
    private Department parent;
    private Employee head;
    private boolean active = true;

    public Department(String name, Department parent) {
        this.name = name;
        this.parent = parent;
    }
}

package com.custom.orm.entity;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.Table;
import com.custom.orm.enums.FieldType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Table(name = "users", schema = "public")
public class User {

    @Id
    private Long id;

    private String firstname;

    private String lastname;

    @Column(name = "birthdate", type = FieldType.DATE)
    private LocalDate birthDate;

    private Integer age;
}

package com.custom.orm.entity;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Entity;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.Table;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.enums.CascadeType;
import com.custom.orm.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", schema = "public")
public class User {

    @Id
    private Long id;

    private String firstname;

    private String lastname;

    @Column(name = "birthdate", type = FieldType.DATE)
    private LocalDate birthDate;

    private Integer age;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;
}

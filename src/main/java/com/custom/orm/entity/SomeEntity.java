package com.custom.orm.entity;

import com.custom.orm.annotations.*;
import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "NewTable")
public class SomeEntity {


    @Id
    @ComposedPrimaryKey
    private int id;

    @Column(name = "FirstName", type = FieldType.VARCHAR, unique = true)
    @ComposedPrimaryKey
    private String name;

    @Column(type = FieldType.BOOLEAN, nullable = false)
    private boolean married;

    @OneToOne
    @JoinColumn(name = "fk_user")
    private User user;
}

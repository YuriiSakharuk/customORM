package com.custom.orm.util;

import com.custom.orm.annotations.*;
import com.custom.orm.enums.FieldType;

import java.util.Objects;

@Table(name = "NewTable")
public class SomeEntity {

    @Column(type = FieldType.INTEGER)
    private int id;

    @Column(name = "FirstName", type = FieldType.VARCHAR)
    private String name;

    @Column(type = FieldType.BOOLEAN)
    private boolean married;

    public SomeEntity(int id, String name, boolean married) {
        this.id = id;
        this.name = name;
        this.married = married;
    }

    public SomeEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMarried() {
        return married;
    }

    public void setMarried(boolean married) {
        this.married = married;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SomeEntity that = (SomeEntity) o;
        return getId() == that.getId() && isMarried() == that.isMarried() && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), isMarried());
    }

    @Override
    public String toString() {
        return "SomeEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", married=" + married +
                '}';
    }
}

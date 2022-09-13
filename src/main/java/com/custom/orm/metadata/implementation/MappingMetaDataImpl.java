package com.custom.orm.metadata.implementation;

import com.custom.orm.annotations.relations.ManyToOne;
import com.custom.orm.annotations.relations.OneToMany;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.enums.CascadeType;
import com.custom.orm.metadata.MappingMetaData;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MappingMetaDataImpl implements MappingMetaData {

    /**
     * This method returns a set of class names of fields of the given entity that are annotated with @OneToOne.
     */
    @Override
    public <T> Set<String> getOneToOneForeignKeyClassNames(Class<T> entityClass) {
        if (Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(OneToOne.class)))

            return Arrays.stream(entityClass.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(OneToOne.class))
                    .filter(field -> Arrays.asList(field.getAnnotation(OneToOne.class).cascade()).contains(CascadeType.ALL) ||
                            Arrays.asList(field.getAnnotation(OneToOne.class).cascade()).contains(CascadeType.GET))
                    .filter(field -> field.getAnnotation(OneToOne.class).mappedBy().length() > 0)
                    .map(field -> field.getType().getName())
                    .collect(Collectors.toSet());

        return new HashSet<>();
    }

    /**
     * This method returns a set of class names of fields of the given entity that are annotated with @OneToMany.
     */
    @Override
    public <T> Set<String> getOneToManyForeignKeyClassNames(Class<T> entityClass) {
        if (Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(OneToMany.class) &&
                        (Arrays.asList(field.getAnnotation(OneToMany.class).cascade()).contains(CascadeType.ALL) ||
                                Arrays.asList(field.getAnnotation(OneToMany.class).cascade()).contains(CascadeType.GET))
                )
        )
            return Arrays.stream(entityClass.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(OneToMany.class))
                    .filter(field -> field.getAnnotation(OneToMany.class).mappedBy().length() > 0)
                    .map(field -> field.getType().getName())
                    .collect(Collectors.toSet());

        return new HashSet<>();
    }

    /**
     * This method returns a set of class names of fields of the given entity that are annotated with @ManyToOne.
     */
    @Override
    public <T> Set<String> getManyToOneForeignKeyClassNames(Class<T> entityClass) {
        if (Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(ManyToOne.class) &&
                        (Arrays.asList(field.getAnnotation(ManyToOne.class).cascade()).contains(CascadeType.ALL) ||
                                Arrays.asList(field.getAnnotation(ManyToOne.class).cascade()).contains(CascadeType.GET))
                )
        )
            return Arrays.stream(entityClass.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                    .filter(field -> field.getAnnotation(ManyToOne.class).mappedBy().length() > 0)
                    .map(field -> field.getType().getName())
                    .collect(Collectors.toSet());

        return new HashSet<>();
    }
}

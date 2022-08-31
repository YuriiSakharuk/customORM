package com.custom.orm;

import com.custom.orm.entity.Profile;
import com.custom.orm.entity.SomeEntity;
import com.custom.orm.entity.User;
import com.custom.orm.mapper.EntitiesMapper;
import com.custom.orm.mapper.EntitiesMapperImpl;
import com.custom.orm.util.TableCreator;

public class Demo {
    public static void main(String[] args) {
        TableCreator tableCreator = new TableCreator();
        EntitiesMapper entitiesMapper = new EntitiesMapperImpl();
        User user = new User();
        SomeEntity someEntity = new SomeEntity();
        Profile profile = new Profile();
        System.out.println(tableCreator.createTableIfNotExists(user));
        System.out.println(tableCreator.createTableIfNotExists(someEntity));
        System.out.println(tableCreator.createTableIfNotExists(profile));
        System.out.println(entitiesMapper.getFindQuery(user.getClass()));
//        System.out.println(entitiesMapper.getFindQuery(someEntity.getClass()));
        System.out.println("~~~~~~~~~");
        System.out.println(entitiesMapper.getFindQuery(profile.getClass()));

//        System.out.println(entitiesMapper.getFindQuery(user.getClass()));
    }
}

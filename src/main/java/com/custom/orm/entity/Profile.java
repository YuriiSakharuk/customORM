package com.custom.orm.entity;

import com.custom.orm.annotations.Entity;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.relations.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    private Long id;

    //@OneToOne
   // @JoinColumn(name = "user_id")
    private User user;

    private String passport;

    @OneToOne
    @JoinColumn(name = "fk_someEntity")
    private SomeEntity someEntity;

    public void setUser(User user) {
        user.setProfile(this);
        this.user = user;
    }
}

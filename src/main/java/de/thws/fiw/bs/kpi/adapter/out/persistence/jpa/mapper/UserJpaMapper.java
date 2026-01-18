package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.UserEntity;
import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;
import de.thws.fiw.bs.kpi.application.domain.model.user.Username;
import jakarta.inject.Singleton;

@Singleton
public class UserJpaMapper implements PersistenceMapper<User, UserEntity> {

    @Override
    public User toDomainModel(UserEntity userEntity) {
        return new User(
                new UserId(userEntity.getId()),
                new Username(userEntity.getUsername()),
                userEntity.getPasswordHash(),
                userEntity.getRole()
        );
    }

    @Override
    public UserEntity toPersistenceModel(User user) {
        return new UserEntity(
                user.getId().value(),
                user.getUsername().value(),
                user.getPasswordHash(),
                user.getRole()
        );
    }
}

package ru.rxnnct.gameapp.core.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import ru.rxnnct.gameapp.core.entity.AppUser;

public interface AppUserRepository extends CrudRepository<AppUser, UUID> {

    Optional<AppUser> findByTgId(Long tgId);

    boolean existsByTgIdAndIsRegisteredTrue(Long tgId);

}
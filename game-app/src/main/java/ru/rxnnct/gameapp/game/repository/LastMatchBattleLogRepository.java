package ru.rxnnct.gameapp.game.repository;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import ru.rxnnct.gameapp.game.entity.LastMatchBattleLog;

public interface LastMatchBattleLogRepository extends CrudRepository<LastMatchBattleLog, UUID> {

}
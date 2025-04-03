package ru.rxnnct.gameapp.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import ru.rxnnct.gameapp.core.entity.AppUser;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_last_match_battle_log", schema = "game_app")
public class LastMatchBattleLog {

    @Id
    @OneToOne
    @JoinColumn(name = "app_user_id", referencedColumnName = "id")
    private AppUser appUser;

    @ManyToOne
    @JoinColumn(name = "match_id", referencedColumnName = "id")
    private Match match;

    @Column(name = "battle_log", columnDefinition = "text")
    private String battleLog;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamp")
    private LocalDateTime updatedAt;
}

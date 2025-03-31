package ru.rxnnct.gameapp.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;
import ru.rxnnct.gameapp.core.entity.AppUser;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_player_rating", schema = "game_app")
public class PlayerRating {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "app_user_id", nullable = false, unique = true)
    private AppUser appUser;

    @Column(name = "mmr")
    private Long mmr;

    @Column(name = "games_played")
    private Long gamesPlayed;

    @Column(name = "wins")
    private Long wins;

    @Column(name = "losses")
    private Long losses;

    @Column(name = "last_updated", columnDefinition = "timestamp")
    private LocalDateTime lastUpdated;

    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}

package ru.rxnnct.gameapp.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import ru.rxnnct.gameapp.core.entity.AppUser;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_game_character", schema = "game_app")
public class GameCharacter {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "max_health")
    private Long maxHealth;

    @Column(name = "strength")
    private Long strength;

    @Column(name = "currency")
    private Long currency;

    @Column(name = "created_at", columnDefinition = "timestamp")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser appUser;
}

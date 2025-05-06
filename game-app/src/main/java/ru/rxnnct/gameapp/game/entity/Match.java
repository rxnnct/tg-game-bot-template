package ru.rxnnct.gameapp.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import ru.rxnnct.gameapp.core.entity.AppUser;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_match", schema = "game_app")
public class Match {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "app_user_id_1", nullable = false, unique = true)
    private AppUser appUser1;

    @OneToOne
    @JoinColumn(name = "app_user_id_2", nullable = false, unique = true)
    private AppUser appUser2;

    @OneToOne
    @JoinColumn(name = "winner", nullable = false, unique = true)
    private AppUser winner;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamp with time zone default now()", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

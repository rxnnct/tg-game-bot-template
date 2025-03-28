package ru.rxnnct.gameapp.core.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.rxnnct.gameapp.game.entity.GameCharacter;
import ru.rxnnct.gameapp.game.entity.PlayerRating;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_app_user", schema = "game_app")
@ToString(exclude = {"characters", "playerRating"})
@EqualsAndHashCode(exclude = {"characters", "playerRating"})
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tg_id", unique = true)
    private Long tgId;

    @Column(name = "name", unique = true, nullable = false, length = 25)
    private String name;

    @Column(name = "is_registered")
    private Boolean isRegistered;

    @Column(name = "balance")
    private Long balance;

    @Column(name = "created_at", columnDefinition = "timestamp")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameCharacter> characters;

    @OneToOne(mappedBy = "appUser", cascade = CascadeType.ALL)
    private PlayerRating playerRating;
}

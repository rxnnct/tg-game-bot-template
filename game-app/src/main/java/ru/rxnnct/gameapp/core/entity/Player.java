package ru.rxnnct.gameapp.core.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.Temporal;
import ru.rxnnct.gameapp.game.entity.GameCharacter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_player", schema = "game_app")
public class Player {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameCharacter> characters;
}

package com.linktic.inventory.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "producto_id", nullable = false, unique = true)
    private UUID productoId;

    @Column(nullable = false)
    private Integer cantidad;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

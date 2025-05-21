package ru.practicum.core.persistance.model;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "categories")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
}

package com.gruporas.watertariffapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias_tarifa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CategoriaTarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaEnum categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tabela_tarifaria_id", nullable = false)
    @JsonBackReference
    private TabelaTarifaria tabelaTarifaria;

    @OneToMany(mappedBy = "categoriaTarifa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @OrderBy("inicio ASC")
    @Builder.Default
    private List<FaixaConsumo> faixas = new ArrayList<>();
}

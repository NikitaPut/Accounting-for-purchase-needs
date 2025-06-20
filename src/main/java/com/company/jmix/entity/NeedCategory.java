package com.company.jmix.entity;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

@JmixEntity
@Table(name = "NEED_CATEGORY")
@Entity
public class NeedCategory {
    // Вид потребности
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TYPE_ID")
    private NeedType type;

    @InstanceName
    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "UNIT")
    private String unit;

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NeedType getType() {
        return type;
    }

    public void setType(NeedType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
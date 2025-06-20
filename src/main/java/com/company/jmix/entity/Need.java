package com.company.jmix.entity;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.flowui.view.ViewController;
import jakarta.persistence.*;

@JmixEntity
@ViewController(id = "Need.list")
@Table(name = "NEED")
@Entity
public class Need {
    // Потребность
    @InstanceName
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERIOD_ID")
    private NeedPeriod period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private NeedCategory category;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "JUSTIFICATION")
    private String justification;

    @Column(name = "APPLICANT")
    private String applicant;

    @Column(name = "APPROVED")
    private Boolean approved = false;

    @Column(name = "ACCOUNTED")
    private Boolean accounted = false;

    @Column(name = "IS_TOTAL")
    private Boolean isTotal = false;

    @Column(name = "CREATED_BY")
    private String createdBy;

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NeedPeriod getPeriod() {
        return period;
    }

    public void setPeriod(NeedPeriod period) {
        this.period = period;
    }

    public NeedCategory getCategory() {
        return category;
    }

    public void setCategory(NeedCategory category) {
        this.category = category;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Boolean getAccounted() {
        return accounted;
    }

    public void setAccounted(Boolean accounted) {
        this.accounted = accounted;
    }

    public Boolean getIsTotal() {
        return isTotal;
    }

    public void setIsTotal(Boolean total) {
        isTotal = total;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
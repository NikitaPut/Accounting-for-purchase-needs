package com.company.jmix.service;

import com.company.jmix.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestDataInitializerService {
    private final DataManager dataManager;
    private final SystemAuthenticator systemAuthenticator;

    public TestDataInitializerService(DataManager dataManager,
                                      SystemAuthenticator systemAuthenticator) {
        this.dataManager = dataManager;
        this.systemAuthenticator = systemAuthenticator;
    }

    @Transactional
    public void createTestNeeds() {
        systemAuthenticator.withSystem(() -> {
            if (!testDataExists()) {
                NeedPeriod period = dataManager.create(NeedPeriod.class);
                period.setName("Тестовый период 2025");
                period.setIsOpen(true);
                NeedPeriod savedPeriod = dataManager.save(period);

                NeedCategory category = dataManager.create(NeedCategory.class);
                category.setName("Тестовая категория");
                NeedCategory savedCategory = dataManager.save(category);

                createTestNeed(savedPeriod, savedCategory, 10, "Тестовая потребность 1");
                createTestNeed(savedPeriod, savedCategory, 5, "Тестовая потребность 2");
            }
            return null;
        });
    }

    private boolean testDataExists() {
        return dataManager.loadValue(
                "select count(n) from Need n where n.justification like 'Тестовая потребность%'",
                Long.class
        ).one() > 0;
    }

    private void createTestNeed(NeedPeriod period, NeedCategory category,
                                int quantity, String justification) {
        Need need = dataManager.create(Need.class);
        need.setPeriod(period);
        need.setCategory(category);
        need.setQuantity(quantity);
        need.setJustification(justification);
        need.setApplicant("Тестовый пользователь");
        dataManager.save(need);
    }
}
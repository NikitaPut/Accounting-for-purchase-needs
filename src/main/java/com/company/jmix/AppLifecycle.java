package com.company.jmix;

import com.company.jmix.service.TestDataInitializerService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AppLifecycle {
    private final TestDataInitializerService testDataInitializerService;

    public AppLifecycle(TestDataInitializerService testDataInitializerService) {
        this.testDataInitializerService = testDataInitializerService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initTestData() {
        testDataInitializerService.createTestNeeds();
    }
}
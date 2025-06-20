package com.company.jmix.service;

import com.company.jmix.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NeedCalculationService {
    private final DataManager dataManager;
    private final Metadata metadata;

    public NeedCalculationService(DataManager dataManager, Metadata metadata) {
        this.dataManager = dataManager;
        this.metadata = metadata;
    }

    @Transactional
    public CalculationResult calculateTotalNeeds(NeedPeriod period) {
        CalculationResult result = new CalculationResult();

        // 1. Удаляем старые итоговые потребности
        List<Need> existingTotals = dataManager.load(Need.class)
                .query("select n from Need n where n.period = :period and n.isTotal = true")
                .parameter("period", period)
                .list();

        result.setRemoved(existingTotals.size());
        existingTotals.forEach(dataManager::remove);

        // 2. Группируем утвержденные потребности по виду
        List<Need> approvedNeeds = dataManager.load(Need.class)
                .query("select n from Need n where n.period = :period and n.approved = true")
                .parameter("period", period)
                .list();

        Map<NeedCategory, Integer> categorySums = new HashMap<>();
        for (Need need : approvedNeeds) {
            categorySums.merge(need.getCategory(), need.getQuantity(), Integer::sum);
        }

        // 3. Создаем новые итоговые записи
        for (Map.Entry<NeedCategory, Integer> entry : categorySums.entrySet()) {
            Need totalNeed = metadata.create(Need.class);
            totalNeed.setPeriod(period);
            totalNeed.setCategory(entry.getKey());
            totalNeed.setQuantity(entry.getValue());
            totalNeed.setIsTotal(true);
            totalNeed.setJustification("Итоговая потребность");
            totalNeed.setCreatedBy("system");

            dataManager.save(totalNeed);
            result.incrementAdded();
        }

        // 4. Помечаем потребности как учтенные
        approvedNeeds.forEach(need -> {
            need.setAccounted(true);
            dataManager.save(need);
        });

        return result;
    }

    public static class CalculationResult {
        private int added;
        private int removed;

        // Getters and setters
        public int getAdded() { return added; }
        public void setAdded(int added) { this.added = added; }
        public void incrementAdded() { this.added++; }
        public int getRemoved() { return removed; }
        public void setRemoved(int removed) { this.removed = removed; }
    }
}
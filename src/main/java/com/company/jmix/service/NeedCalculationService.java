package com.company.jmix.service;

import com.company.jmix.entity.Need;
import com.company.jmix.entity.NeedCategory;
import com.company.jmix.entity.NeedPeriod;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NeedCalculationService {

    public static class CalculationResult {
        private final String added;
        private final String removed;
        private final String updated;

        public CalculationResult(int added, int removed, int updated) {
            this.added = String.valueOf(added);
            this.removed = String.valueOf(removed);
            this.updated = String.valueOf(updated);
        }

        public String getAdded() { return added; }
        public String getRemoved() { return removed; }
        public String getUpdated() { return updated; }
    }

    @Autowired
    private DataManager dataManager;

    public CalculationResult calculateTotalNeeds(NeedPeriod period) {
        if (period == null) {
            throw new IllegalArgumentException("Period cannot be null");
        }

        int added = 0;
        int removed = 0;
        int updated = 0;

        // 1. Получаем все текущие итоговые потребности для периода
        List<Need> existingTotals = dataManager.load(Need.class)
                .query("SELECT n FROM Need n WHERE n.period = :period AND n.isTotal = true")
                .parameter("period", period)
                .list();

        // 2. Получаем все обычные потребности для периода (только утвержденные)
        List<Need> regularNeeds = dataManager.load(Need.class)
                .query("SELECT n FROM Need n WHERE n.period = :period AND n.isTotal = false AND n.approved = true")
                .parameter("period", period)
                .list();

        // 3. Группируем обычные потребности по категориям (игнорируя нулевые категории)
        Map<NeedCategory, Integer> categorySums = new HashMap<>();
        for (Need regularNeed : regularNeeds) {
            if (regularNeed != null && regularNeed.getCategory() != null && regularNeed.getQuantity() != null) {
                categorySums.merge(
                        regularNeed.getCategory(),
                        regularNeed.getQuantity(),
                        Integer::sum
                );
            }
        }

        // 4. Обновляем или создаем итоговые потребности
        for (Map.Entry<NeedCategory, Integer> entry : categorySums.entrySet()) {
            NeedCategory category = entry.getKey();
            Integer totalQuantity = entry.getValue();

            if (category == null || totalQuantity == null) {
                continue;
            }

            Optional<Need> existingTotal = existingTotals.stream()
                    .filter(n -> n != null && n.getCategory() != null && n.getCategory().equals(category))
                    .findFirst();

            if (existingTotal.isPresent()) {
                Need total = existingTotal.get();
                if (!totalQuantity.equals(total.getQuantity())) {
                    total.setQuantity(totalQuantity);
                    dataManager.save(total);
                    updated++;
                }
            } else {
                Need newTotal = dataManager.create(Need.class);
                newTotal.setPeriod(period);
                newTotal.setCategory(category);
                newTotal.setQuantity(totalQuantity);
                newTotal.setIsTotal(true);
                newTotal.setApproved(false);
                dataManager.save(newTotal);
                added++;
            }
        }

        // 5. Удаляем устаревшие итоговые потребности
        for (Need total : existingTotals) {
            if (total != null && total.getCategory() != null && !categorySums.containsKey(total.getCategory())) {
                dataManager.remove(total);
                removed++;
            }
        }

        return new CalculationResult(added, removed, updated);
    }
}
package com.company.jmix.view.needdetail;

import com.company.jmix.entity.Need;
import com.company.jmix.entity.NeedPeriod;
import com.company.jmix.entity.NeedCategory;
import com.company.jmix.service.TestDataInitializerService;
import com.company.jmix.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@ViewController("Need.detail")
@ViewDescriptor("need-detail-view.xml")
@EditedEntityContainer("needDc")
@Route(value = "need-detail-list", layout = MainView.class)
public class NeedDetailView extends StandardDetailView<Need> {

    @ViewComponent
    private InstanceContainer<Need> needDc;

    @ViewComponent
    private CollectionContainer<NeedPeriod> periodsDc;

    @ViewComponent
    private CollectionContainer<NeedCategory> categoriesDc;

    @ViewComponent
    private JmixComboBox<Integer> periodField;

    @ViewComponent
    private JmixComboBox<Integer> categoryField;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private TestDataInitializerService testDataService;

    @Subscribe
    public void onInit(InitEvent event) {
        periodField.setItems();
        categoryField.setItems();
        loadPeriods();
        loadCategories();

        // Загрузка категорий
        List<NeedCategory> categories = dataManager.load(NeedCategory.class)
                .query("select c from NeedCategory c order by c.name")
                .list();
        categoriesDc.setItems(categories);
    }

    private void loadPeriods() {
        List<NeedPeriod> periods = dataManager.load(NeedPeriod.class)
                        .query("select p from NeedPeriod p order by p.id desc")
                        .fetchPlan(FetchPlan.INSTANCE_NAME)
                        .list();
        System.out.println("Loaded periods: " + periods.size());
        periodsDc.setItems(periods);
    }

    private void loadCategories() {
        // Загрузка категорий
        List<NeedCategory> categories = dataManager.load(NeedCategory.class)
                .query("select c from NeedCategory c order by c.name")
                .list();
        System.out.println("Loaded categories: " + categories.size());
        categoriesDc.setItems(categories);
    }

    /*@Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Need need = getEditedEntity();
        if (need.getId() == null) {
            initializeNewNeed(need);
            // Устанавливаем категорию по умолчанию
            NeedCategory defaultCategory = dataManager.load(NeedCategory.class)
                    .query("select c from NeedCategory c where c.isDefault = true")
                    .one();
            EntityValues.setValue(defaultCategory, "+truckLoadCapacity", defaultCategory);
            need.setCategory(defaultCategory);
        }

        // Дополнительная проверка (на случай если загрузка в onInit не сработала)
        if (periodsDc.getItems().isEmpty()) {
            loadPeriods();
        }
        if (categoriesDc.getItems().isEmpty()) {
            loadCategories();
        }
    }*/

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Need need = getEditedEntity();
        if (need.getId() == null) {
            initializeNewNeed(need);
            // Try to load default category
            Optional<NeedCategory> defaultCategory = dataManager.load(NeedCategory.class)
                    .query("select c from NeedCategory c where c.isDefault = true")
                    .optional();
            if (defaultCategory.isPresent()) {
                need.setCategory(defaultCategory.get());
            } else {
                // Fallback: Load first available category or handle absence
                List<NeedCategory> categories = dataManager.load(NeedCategory.class)
                        .query("select c from NeedCategory c order by c.name")
                        .list();
                if (!categories.isEmpty()) {
                    need.setCategory(categories.get(0)); // Set first category as fallback
                } else {
                    // Log warning or notify user that no categories exist
                    System.out.println("No categories available to set as default.");
                }
            }
        }

        // Ensure periods and categories are loaded
        if (periodsDc.getItems().isEmpty()) {
            loadPeriods();
        }
        if (categoriesDc.getItems().isEmpty()) {
            loadCategories();
        }
    }

    private void initializeNewNeed(Need need) {
        need.setApproved(false);
        need.setAccounted(false);
        need.setIsTotal(false);
    }

    private void setLatestPeriod(Need need) {
        Optional<NeedPeriod> latestPeriod = dataManager.load(NeedPeriod.class)
                .query("select p from NeedPeriod p order by p.id desc")
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .optional();
        latestPeriod.ifPresent(need::setPeriod);
    }

    @Subscribe
    public void onValidation(ValidationEvent event) {
        Need need = getEditedEntity();
        if (need.getQuantity() != null && need.getQuantity() <= 0) {
            event.getErrors().add("Количество должно быть положительным");
        }
        if (need.getPeriod() == null) {
            event.getErrors().add("Период должен быть указан");
        }
        if (need.getCategory() == null) {
            event.getErrors().add("Категория должна быть указана");
        }
    }
}
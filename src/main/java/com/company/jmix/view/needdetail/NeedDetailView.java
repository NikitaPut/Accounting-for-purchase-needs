package com.company.jmix.view.needdetail;

import com.company.jmix.entity.Need;
import com.company.jmix.entity.NeedPeriod;
import com.company.jmix.entity.NeedCategory;
import com.company.jmix.service.TestDataInitializerService;
import com.company.jmix.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
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
    private InstanceLoader<Need> needDl;

    @ViewComponent
    private CollectionContainer<NeedPeriod> periodsDc;

    @ViewComponent
    private CollectionContainer<NeedCategory> categoriesDc;

    @ViewComponent
    private JmixComboBox<NeedPeriod> periodField;

    @ViewComponent
    private JmixComboBox<NeedCategory> categoryField;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private TestDataInitializerService testDataService;

    @Subscribe
    public void onInit(InitEvent event) {
        loadPeriods();
        loadCategories();
        periodField.setItems(periodsDc.getItems());
        categoryField.setItems(categoriesDc.getItems());
    }

    private void loadPeriods() {
        List<NeedPeriod> periods = dataManager.load(NeedPeriod.class)
                .query("select p from NeedPeriod p order by p.id desc")
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .list();
        periodsDc.setItems(periods);
    }

    private void loadCategories() {
        List<NeedCategory> categories = dataManager.load(NeedCategory.class)
                .query("select c from NeedCategory c order by c.name")
                .list();
        categoriesDc.setItems(categories);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Need need = getEditedEntity();
        if (need.getId() != null) {
            needDl.setEntityId(need.getId());
            needDl.load();
        }
        if (need.getId() == null) {
            initializeNewNeed(need);
            setLatestPeriod(need);
            if (need.getCategory() == null) {
                NeedCategory defaultCategory = testDataService.getDefaultCategory();
                need.setCategory(defaultCategory);
            }
        }

        if (periodsDc.getItems().isEmpty()) {
            loadPeriods();
            periodField.setItems(periodsDc.getItems());
        }
        if (categoriesDc.getItems().isEmpty()) {
            loadCategories();
            categoryField.setItems(categoriesDc.getItems());
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

    @Subscribe("saveAction")
    public void onSaveAction(ActionPerformedEvent event) {
        // Проверяем валидность формы
        ValidationErrors errors = validateView();

        if (errors.isEmpty()) {
            // Если ошибок нет - сохраняем и закрываем
            Need need = getEditedEntity();
            dataManager.save(need);
            close(StandardOutcome.SAVE);
        } else {
            // Показываем ошибки пользователю
        }
    }

    @Subscribe("closeAction")
    public void onCloseAction(ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }
}
package com.company.jmix.view.needdetail;

import com.company.jmix.entity.Need;
import com.company.jmix.entity.NeedPeriod;
import com.company.jmix.entity.NeedCategory;
import com.company.jmix.service.TestDataInitializerService;
import com.company.jmix.view.main.MainView;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.component.combobox.JmixComboBox;
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

        // Set items for combo boxes (safe to do in onInit as it doesn't depend on edited entity)
        periodField.setItems(periodsDc.getItems());
        categoryField.setItems(categoriesDc.getItems());
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
        List<NeedCategory> categories = dataManager.load(NeedCategory.class)
                .query("select c from NeedCategory c order by c.name")
                .list();
        System.out.println("Loaded categories: " + categories.size());
        categoriesDc.setItems(categories);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
// Убедитесь, что загрузчик инициализирован перед загрузкой данных
        Need need = getEditedEntity();
        if (need.getId() != null) {
            needDl.setEntityId(need.getId());
            needDl.load();
        }        if (need.getId() == null) {
            initializeNewNeed(need);
            setLatestPeriod(need);
            // Preselect default category if not set
            if (need.getCategory() == null) {
                NeedCategory defaultCategory = testDataService.getDefaultCategory();
                need.setCategory(defaultCategory);
            }
        }

        // Ensure combo boxes are populated
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

//    @Subscribe("saveAction")
//    public void onSaveAction(ActionPerformedEvent event) {
//        try {
//            if (saveChanges()) {  // Нужно исправить saveChanges
//                close(StandardOutcome.SAVE);
//            }
//        } catch (ValidationException e) {
//            // Ошибки уже показаны пользователю
//        }
//    }

    @Subscribe("closeAction")
    public void onCloseAction(ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }
}

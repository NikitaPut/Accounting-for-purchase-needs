package com.company.jmix.view.needdetail;

import com.company.jmix.entity.Need;
import com.company.jmix.entity.NeedPeriod;
import com.company.jmix.entity.NeedCategory;
import com.company.jmix.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@ViewController("Need.detail")
@ViewDescriptor("need-detail-view.xml")
@EditedEntityContainer("needDc")
@Route(value = "need-detail-list", layout = MainView.class)
public class NeedDetailView extends StandardDetailView<Need> {

    private DialogWindow<NeedDetailView> dialogWindow;

    @ViewComponent
    private InstanceContainer<Need> needDc;

    @ViewComponent
    private CollectionContainer<NeedPeriod> periodsDc;

    @ViewComponent
    private CollectionContainer<NeedCategory> categoriesDc;

    @Autowired
    private DataManager dataManager;

    @Subscribe
    public void onInit(InitEvent event) {
        loadPeriods();
        loadCategories();
    }

    @Subscribe("closeBtn")
    public void onCloseBtnClick(ClickEvent<Button> event) {
        dialogWindow.close();
    }

    public void setDialogWindow(DialogWindow<NeedDetailView> dialogWindow) {
        this.dialogWindow = dialogWindow;
    }

    private void loadPeriods() {
        periodsDc.setItems(dataManager.load(NeedPeriod.class)
                .query("select p from NeedPeriod p order by p.id desc")
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .list());
    }

    private void loadCategories() {
        categoriesDc.setItems(dataManager.load(NeedCategory.class)
                .query("select c from NeedCategory c order by c.name")
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .list());
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Need need = getEditedEntity();

        if (need.getId() == null) {
            initializeNewNeed(need);
        }
    }

    private void initializeNewNeed(Need need) {
        need.setApproved(false);
        need.setAccounted(false);
        need.setIsTotal(false);
        setLatestPeriod(need);
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
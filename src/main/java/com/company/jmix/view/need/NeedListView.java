package com.company.jmix.view.need;

import com.company.jmix.entity.Need;
import com.company.jmix.entity.NeedPeriod;
import com.company.jmix.entity.NeedCategory;
import com.company.jmix.service.NeedCalculationService;
import com.company.jmix.view.main.MainView;
import com.company.jmix.view.needdetail.NeedDetailView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ViewController("Need.browse")
@ViewDescriptor("need-list-view.xml")
@Route(value = "need-list", layout = MainView.class)
public class NeedListView extends StandardListView<Need> {

    @ViewComponent
    private DataGrid<Need> needsDataGrid;

    @ViewComponent
    private CollectionLoader<Need> needsDl;

    @ViewComponent
    private CollectionContainer<Need> needsDc;

    @Autowired
    private DataManager dataManager;
    @Autowired
    private NeedCalculationService needCalculationService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Messages messages;
    @Autowired
    private DialogWindow dialogWindowManager;

    @Subscribe
    public void onInit(InitEvent event) {
        // Начальная загрузка данных
        needsDl.load();
    }

    @Subscribe("generateTotalAction")
    public void onGenerateTotal(ActionPerformedEvent event) {
        Need selected = needsDataGrid.getSingleSelectedItem();
        if (selected == null || selected.getPeriod() == null) {
            notifications.show(messages.getMessage("need.selectWithPeriod"));
            return;
        }

        NeedPeriod period = selected.getPeriod();
        if (!period.getIsOpen()) {
            notifications.show(messages.getMessage("need.periodClosed"));
            return;
        }

        NeedCalculationService.CalculationResult result = needCalculationService.calculateTotalNeeds(period);
        String message = String.format(messages.getMessage("need.calculationResult"), result.getAdded(), result.getRemoved());
        notifications.show(message);
        needsDl.load();
    }

    @Subscribe("approveAction")
    public void onApprove(ActionPerformedEvent event) {
        Need need = needsDataGrid.getSingleSelectedItem();
        if (need == null) {
            notifications.show(messages.getMessage("need.select"));
            return;
        }

        need.setApproved(!need.getApproved());

        if (need.getApproved() && need.getPeriod().getIsOpen() && hasTotalForPeriod(need.getPeriod())) {
            notifications.show(messages.getMessage("need.notAccounted"));
        }

        dataManager.save(need);
        needsDl.load();
    }

    @Subscribe("createAction")
    public void onCreateAction(ActionPerformedEvent event) {
        Optional<NeedPeriod> lastPeriod = dataManager.load(NeedPeriod.class)
                .query("select p from NeedPeriod p order by p.id desc")
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .optional();

        if (lastPeriod.isEmpty()) {
            notifications.show(messages.getMessage("need.noPeriodCreate"));
            return;
        }

        Need newNeed = dataManager.create(Need.class);
        newNeed.setPeriod(lastPeriod.get());

        // Предварительно загружаем данные для диалога
        List<NeedPeriod> periods = dataManager.load(NeedPeriod.class)
                .query("select p from NeedPeriod p order by p.id desc")
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .list();
        List<NeedCategory> categories = dataManager.load(NeedCategory.class)
                .query("select c from Category c order by c.name")
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .list();
    }

    @Subscribe("exportAction")
    public void onExportAction(ActionPerformedEvent event) {
        if (needsDc.getItems().isEmpty()) {
            notifications.show(messages.getMessage("need.noData"));
            return;
        }

        try {
//            ExcelExporter excelExporter = applicationContext.getBean(ExcelExporter.class);
//            excelExporter.setFileName("Needs_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".xlsx");
//            excelExporter.exportDataGrid(needsDataGrid);
            notifications.show(messages.getMessage("need.exportSuccess"));
        } catch (Exception e) {
            notifications.show(messages.formatMessage("need.exportError", e.getMessage()));
        }
    }

    private boolean hasTotalForPeriod(NeedPeriod period) {
        Long count = dataManager.loadValue(
                        "select count(n) from Need n where n.period = :period and n.isTotal = true", Long.class)
                .parameter("period", period)
                .one();
        return count > 0;
    }

    @Install(to = "needsDataGrid.remove", subject = "enabledRule")
    private boolean needsDataGridRemoveEnabledRule() {
        Need selected = needsDataGrid.getSingleSelectedItem();
        return selected != null && !selected.getApproved() && !selected.getAccounted();
    }
}

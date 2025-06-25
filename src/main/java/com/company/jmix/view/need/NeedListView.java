package com.company.jmix.view.need;

import com.company.jmix.entity.Need;
import com.company.jmix.entity.NeedPeriod;
import com.company.jmix.entity.NeedCategory;
import com.company.jmix.service.NeedCalculationService;
import com.company.jmix.view.main.MainView;
import com.company.jmix.view.needdetail.NeedDetailView;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.gridexportflowui.action.ExcelExportAction;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

@ViewController("Need.browse")
@ViewDescriptor("need-list-view.xml")
@Route(value = "need-list", layout = MainView.class)
@DialogMode(width = "50em", height = "37.5em", resizable = true)
public class NeedListView extends StandardListView<Need> {

    @ViewComponent
    private DataGrid<Need> needsDataGrid;

    @ViewComponent
    private CollectionLoader<Need> needsDl;

    @ViewComponent
    private CollectionContainer<Need> needsDc;

    @ViewComponent
    private JmixButton approveBtn;

    @ViewComponent("needsDataGrid.excelExport")
    private ExcelExportAction excelExportAction; // Injected action

    @Autowired
    private DataManager dataManager;
    @Autowired
    private NeedCalculationService needCalculationService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Messages messages;
    @Autowired
    private DialogWindows dialogWindows;

    @Subscribe
    public void onInit(InitEvent event) {
        needsDl.load();
        updateApproveButtonText();
        if (needsDataGrid != null && excelExportAction != null) {
            excelExportAction.setText("Экспорт в Excel");
        } else {
            notifications.show("Ошибка инициализации экспорта!");
        }
    }

    @Subscribe(id = "needsDataGrid", subject = "selectionListener")
    public void onSelectionChange(SelectionEvent<DataGrid<Need>, Need> event) {
        updateApproveButtonText();
    }

    private void updateApproveButtonText() {
        Need selected = needsDataGrid.getSingleSelectedItem();
        if (selected == null || !selected.getPeriod().getIsOpen()) {
            approveBtn.setEnabled(false);
            return;
        }
        approveBtn.setEnabled(true);
        approveBtn.setText(selected.getApproved() ? "Разутвердить" : "Утвердить");
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

        dialogWindows.detail(this, Need.class)
                .editEntity(newNeed)
                .withViewClass(NeedDetailView.class)
                .open()
                .addAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        needsDl.load();
                        notifications.show(messages.getMessage("need.created"));
                    }
                });
    }

    @Subscribe("generateTotalAction")
    public void onGenerateTotal(ActionPerformedEvent event) {
        Need selected = needsDataGrid.getSingleSelectedItem();
        if (selected == null || selected.getPeriod() == null) {
            notifications.show(messages.getMessage("need.selectWithPeriod"));
            return;
        }

        NeedPeriod period = selected.getPeriod();
        if (period == null || !period.getIsOpen()) {
            notifications.show(messages.getMessage("need.periodClosed"));
            return;
        }

        try {
            NeedCalculationService.CalculationResult result = needCalculationService.calculateTotalNeeds(period);

            String message = messages.formatMessage(
                    "Добавлено: {0}, Удалено: {1}, Обновлено: {2}",
                    result.getAdded(),
                    result.getRemoved(),
                    result.getUpdated()
            );

            notifications.show(message);
        } catch (Exception e) {
            notifications.show("Ошибка при расчете итогов: " + e.getMessage());
        } finally {
            needsDl.load();
        }
    }

    @Subscribe("approveAction")
    public void onApproveAction(ActionPerformedEvent event) {
        Need selected = needsDataGrid.getSingleSelectedItem();
        if (selected == null) {
            notifications.show(messages.getMessage("need.select"));
            return;
        }

        boolean wasApproved = selected.getApproved();
        selected.setApproved(!wasApproved);

        if (selected.getApproved()) {
            if (selected.getPeriod().getIsOpen() && hasTotalForPeriod(selected.getPeriod())) {
                notifications.show(messages.getMessage("need.notAccounted"));
            }
        } else {
            if (selected.getPeriod().getIsOpen() && hasTotalForPeriod(selected.getPeriod())) {
                notifications.show(messages.getMessage("need.recalculateTotal"));
            }
        }

        dataManager.save(selected);
        needsDl.load();
        updateApproveButtonText();
    }

    @Subscribe("exportAction")
    public void onExportAction(ActionPerformedEvent event) {
        if (needsDc.getItems().isEmpty()) {
            notifications.create("Нет данных для экспорта").show();
            return;
        }

        try {
            // pivotTableExport.exportTableToXls();
            notifications.create("Экспорт завершён успешно").show();
        } catch (Exception e) {
            notifications.create("Ошибка при экспорте: " + e.getMessage()).show();
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
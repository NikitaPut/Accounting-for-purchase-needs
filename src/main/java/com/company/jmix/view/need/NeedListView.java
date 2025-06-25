package com.company.jmix.view.need;

import com.company.jmix.entity.Need;
import com.company.jmix.entity.NeedPeriod;
import com.company.jmix.entity.NeedCategory;
import com.company.jmix.service.NeedCalculationService;
import com.company.jmix.view.main.MainView;
import com.company.jmix.view.needdetail.NeedDetailView;
import com.vaadin.flow.component.ClickEvent;
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
// import io.jmix.pivottableflowui.component.PivotTable;
// import org.docx4j.openpackaging.parts.SpreadsheetML.PivotTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

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
    // private PivotTable pivotTable;
    // private PivotTableExporter pivotTableExport;

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
    @Autowired
    private ApplicationContext applicationContext;

    @Subscribe
    public void onInit(InitEvent event) {
        needsDl.load();
//        PivotTableExporterExcele exporter = getApplicationContext()
//                .getBean(String.valueOf(PivotTableExcelExporter.class));
//        pivotTableExport = getApplicationContext()
//                .getBean(PivotTableExporter.class, pivotTable, pivotTableExport);

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
        if (!period.getIsOpen()) {
            notifications.show(messages.getMessage("need.periodClosed"));
            return;
        }

        NeedCalculationService.CalculationResult result = needCalculationService.calculateTotalNeeds(period);

        String message = messages.formatMessage(
                "Добавлено: {0}, Удалено: {1}, Обновлено: {2}",
                result.getAdded(),
                result.getRemoved(),
                result.getUpdated()
        );

        notifications.show(message);
        needsDl.load();
    }

    @Subscribe("approveAction")
    public void onApprove(ActionPerformedEvent event) {
        Need selected = needsDataGrid.getSingleSelectedItem();
        if (selected == null) {
            notifications.show(messages.getMessage("need.select"));
            return;
        }

        selected.setApproved(!selected.getApproved());

        if (selected.getApproved() && selected.getPeriod().getIsOpen() && hasTotalForPeriod(selected.getPeriod())) {
            notifications.show(messages.getMessage("need.notAccounted"));
        }

        dataManager.save(selected);
        needsDl.load();
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
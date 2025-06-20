package com.company.jmix.view.needdetail;

<<<<<<< HEAD
import com.company.jmix.entity.*;
import com.company.jmix.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.model.*;
=======
import com.company.jmix.entity.Need;
import com.company.jmix.entity.NeedPeriod;
import com.company.jmix.entity.NeedCategory;
import com.company.jmix.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
>>>>>>> 59c5684a9f8e20cd85a76c94f451d5f52e5a3e6e
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@ViewController("Need.detail")
@ViewDescriptor("need-detail-view.xml")
@EditedEntityContainer("needDc")
@Route(value = "need-detail-list", layout = MainView.class)
public class NeedDetailView extends StandardDetailView<Need> {

<<<<<<< HEAD
    private DialogWindow<NeedDetailView> dialogWindow;

=======
>>>>>>> 59c5684a9f8e20cd85a76c94f451d5f52e5a3e6e
    @ViewComponent
    private InstanceContainer<Need> needDc;

    @ViewComponent
<<<<<<< HEAD
=======
    private CollectionContainer<NeedPeriod> periodsDc;

    @ViewComponent
>>>>>>> 59c5684a9f8e20cd85a76c94f451d5f52e5a3e6e
    private CollectionContainer<NeedCategory> categoriesDc;

    @Autowired
    private DataManager dataManager;

    @Subscribe
    public void onInit(InitEvent event) {
<<<<<<< HEAD
        loadCategories();
    }

    @Subscribe("closeBtn")
    public void onCloseBtnClick(ClickEvent<Button> event) {
        dialogWindow.close();
    }

    public void setDialogWindow(DialogWindow<NeedDetailView> dialogWindow) {
        this.dialogWindow = dialogWindow;
    }

    private void loadCategories() {
        categoriesDc.setItems(dataManager.load(NeedCategory.class)
                .query("select c from NeedCategory c order by c.name")
=======
        // Загрузка данных с предопределенным fetchPlan
        loadPeriods();
        loadCategories();
    }

    private void loadPeriods() {
        // Используем предопределенный fetchPlan _instance_name, который включает базовые атрибуты
        periodsDc.setItems(dataManager.load(NeedPeriod.class)
                .query("select p from NeedPeriod p order by p.id desc")
                .fetchPlan(FetchPlan.INSTANCE_NAME) // _instance_name включает name
                .list());
    }

    private void loadCategories() {
        // Используем предопределенный fetchPlan _instance_name, который включает name
        categoriesDc.setItems(dataManager.load(NeedCategory.class)
                .query("select c from NeedCategory c order by c.name")
                .fetchPlan(FetchPlan.INSTANCE_NAME) // _instance_name включает name
>>>>>>> 59c5684a9f8e20cd85a76c94f451d5f52e5a3e6e
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

        // Установка последнего периода
        setLatestPeriod(need);
    }

    private void setLatestPeriod(Need need) {
<<<<<<< HEAD
        dataManager.load(NeedPeriod.class)
                .query("select p from NeedPeriod p order by p.id desc")
                .maxResults(1)
                .optional()
                .ifPresent(need::setPeriod);
=======
        // Загружаем с предопределенным fetchPlan
        Optional<NeedPeriod> latestPeriod = dataManager.load(NeedPeriod.class)
                .query("select p from NeedPeriod p order by p.id desc")
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .optional();

        latestPeriod.ifPresent(need::setPeriod);
>>>>>>> 59c5684a9f8e20cd85a76c94f451d5f52e5a3e6e
    }

    @Subscribe
    public void onValidation(ValidationEvent event) {
        Need need = getEditedEntity();

<<<<<<< HEAD
        validateQuantity(need, event);
        validatePeriod(need, event);
        validateCategory(need, event);
    }

    private void validateQuantity(Need need, ValidationEvent event) {
        if (need.getQuantity() != null && need.getQuantity() <= 0) {
            event.getErrors().add("Количество должно быть положительным");
        }
    }

    private void validatePeriod(Need need, ValidationEvent event) {
        if (need.getPeriod() == null) {
            event.getErrors().add("Необходимо указать период");
        }
    }

    private void validateCategory(Need need, ValidationEvent event) {
        if (need.getCategory() == null) {
            event.getErrors().add("Необходимо указать категорию");
        }
    }
}
=======
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
>>>>>>> 59c5684a9f8e20cd85a76c94f451d5f52e5a3e6e

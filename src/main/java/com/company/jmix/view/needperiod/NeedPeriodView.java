package com.company.jmix.view.needperiod;

import com.company.jmix.entity.NeedPeriod;
import com.company.jmix.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "need-periods", layout = MainView.class)
@ViewController(id = "NeedPeriod.list")
@ViewDescriptor(path = "need-period-list-view.xml")
@LookupComponent("needPeriodsDataGrid")
@DialogMode(width = "64em")
public class NeedPeriodView extends StandardListView<NeedPeriod> {
}
package com.company.jmix.view.needcategory;

import com.company.jmix.entity.NeedCategory;
import com.company.jmix.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "need-categories", layout = MainView.class)
@ViewController(id = "NeedCategory.list")
@ViewDescriptor(path = "need-category-list-view.xml")
@LookupComponent("needCategoriesDataGrid")
@DialogMode(width = "64em")
public class NeedCategoryView extends StandardListView<NeedCategory> {
}
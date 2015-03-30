package org.bbop.apollo.gwt.client;

import com.google.gwt.cell.client.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.bbop.apollo.UserOrganismPermission;
import org.bbop.apollo.gwt.client.dto.UserInfo;
import org.bbop.apollo.gwt.client.dto.UserOrganismPermissionInfo;
import org.bbop.apollo.gwt.client.event.UserChangeEvent;
import org.bbop.apollo.gwt.client.event.UserChangeEventHandler;
import org.bbop.apollo.gwt.client.resources.TableResources;
import org.bbop.apollo.gwt.client.rest.UserRestService;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.IconType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by ndunn on 12/17/14.
 */
public class UserPanel extends Composite {

    interface UserBrowserPanelUiBinder extends UiBinder<Widget, UserPanel> {
    }

    private static UserBrowserPanelUiBinder ourUiBinder = GWT.create(UserBrowserPanelUiBinder.class);
    //    @UiField
//    HTML userGroupHTML;
    @UiField
    TextBox firstName;
    @UiField
    TextBox lastName;
    @UiField
    TextBox email;

    DataGrid.Resources tablecss = GWT.create(TableResources.TableCss.class);
    @UiField(provided = true)
    DataGrid<UserInfo> dataGrid = new DataGrid<UserInfo>(10, tablecss);
    @UiField
    Button createButton;
    @UiField
    Button cancelButton;
    @UiField
    Button deleteButton;
    @UiField
    Button saveButton;
    @UiField
    PasswordTextBox passwordTextBox;
    @UiField
    Row passwordRow;
    @UiField
    ListBox roleList;
    @UiField
    FlexTable groupTable;
    @UiField
    org.gwtbootstrap3.client.ui.ListBox availableGroupList;
    @UiField
    org.gwtbootstrap3.client.ui.Button addGroupButton;
    @UiField
    TabLayoutPanel userDetailTab;
    //    @UiField(provided = true)
//    com.google.gwt.user.client.ui.DataGrid<UserOrganismPermissionInfo> organismPermissionsGrid;
//    @UiField FlexTable organismPermissions;
    @UiField(provided = true)
    DataGrid<UserOrganismPermissionInfo> organismPermissionsGrid = new DataGrid<>(4,tablecss);


    private ListDataProvider<UserInfo> dataProvider = new ListDataProvider<>();
    private List<UserInfo> userInfoList = dataProvider.getList();
    private SingleSelectionModel<UserInfo> selectionModel = new SingleSelectionModel<>();
    private UserInfo selectedUserInfo;

    private ListDataProvider<UserOrganismPermissionInfo> permissionProvider = new ListDataProvider<>();
    private List<UserOrganismPermissionInfo> permissionProviderList = permissionProvider.getList();
    private ColumnSortEvent.ListHandler<UserOrganismPermissionInfo> sortHandler = new ColumnSortEvent.ListHandler<UserOrganismPermissionInfo>(permissionProviderList);

    public UserPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));

//        userGroupHTML.setHTML("<div class='label label-default'>USDA i5K</div>");

        // TODO: grab from server or use constants
        if (roleList.getItemCount() == 0) {
            roleList.addItem("user");
            roleList.addItem("admin");
        }


        TextColumn<UserInfo> firstNameColumn = new TextColumn<UserInfo>() {
            @Override
            public String getValue(UserInfo employee) {
                return employee.getName();
            }
        };
        firstNameColumn.setSortable(true);

        SafeHtmlRenderer<String> anchorRenderer = new AbstractSafeHtmlRenderer<String>() {
            @Override
            public SafeHtml render(String object) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendHtmlConstant("<a href=\"javascript:;\">").appendEscaped(object)
                        .appendHtmlConstant("</a>");
                return sb.toSafeHtml();
            }
        };

        Column<UserInfo, String> secondNameColumn = new Column<UserInfo, String>(new ClickableTextCell(anchorRenderer)) {
            @Override
            public String getValue(UserInfo employee) {
                return employee.getEmail();
            }
        };
        secondNameColumn.setSortable(true);

        TextColumn<UserInfo> thirdNameColumn = new TextColumn<UserInfo>() {
            @Override
            public String getValue(UserInfo employee) {
                return employee.getRole();
            }
        };
        thirdNameColumn.setSortable(true);

        dataGrid.addColumn(firstNameColumn, "Name");
        dataGrid.addColumn(secondNameColumn, "Email");
        dataGrid.addColumn(thirdNameColumn, "Global Role");

        dataGrid.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                selectedUserInfo = selectionModel.getSelectedObject();
                updateUserInfo();
            }
        });

        dataProvider.addDataDisplay(dataGrid);


        createOrganismPermissionsTable();


        ColumnSortEvent.ListHandler<UserInfo> sortHandler = new ColumnSortEvent.ListHandler<UserInfo>(userInfoList);
        dataGrid.addColumnSortHandler(sortHandler);
        sortHandler.setComparator(firstNameColumn, new Comparator<UserInfo>() {
            @Override
            public int compare(UserInfo o1, UserInfo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        sortHandler.setComparator(secondNameColumn, new Comparator<UserInfo>() {
            @Override
            public int compare(UserInfo o1, UserInfo o2) {
                return o1.getEmail().compareTo(o2.getEmail());
            }
        });
        sortHandler.setComparator(thirdNameColumn, new Comparator<UserInfo>() {
            @Override
            public int compare(UserInfo o1, UserInfo o2) {
                return o1.getRole().compareTo(o2.getRole());
            }
        });

        Annotator.eventBus.addHandler(UserChangeEvent.TYPE, new UserChangeEventHandler() {
            @Override
            public void onUserChanged(UserChangeEvent userChangeEvent) {
                switch (userChangeEvent.getAction()) {
                    case ADD_USER_TO_GROUP:
                        availableGroupList.removeItem(availableGroupList.getSelectedIndex());
                        if (availableGroupList.getItemCount() > 0) {
                            availableGroupList.setSelectedIndex(0);
                        }
                        String group = userChangeEvent.getGroup();
                        addGroupToUi(group);
                        break;
                    case RELOAD_USERS:
                        reload();
                        break;
                    case REMOVE_USER_FROM_GROUP:
                        removeGroupFromUI(userChangeEvent.getGroup());
                        break;

                }
            }
        });
    }

    private void createOrganismPermissionsTable() {
        TextColumn<UserOrganismPermissionInfo> organismNameColumn = new TextColumn<UserOrganismPermissionInfo>() {
            @Override
            public String getValue(UserOrganismPermissionInfo userOrganismPermissionInfo) {
                return userOrganismPermissionInfo.getOrganismName();
            }
        };
        organismNameColumn.setSortable(true);
        organismNameColumn.setDefaultSortAscending(true);


        Column<UserOrganismPermissionInfo, Boolean> adminColumn = new Column<UserOrganismPermissionInfo, Boolean>(new CheckboxCell(true, true)) {
            @Override
            public Boolean getValue(UserOrganismPermissionInfo object) {
                return object.isAdmin();
            }
        };
        adminColumn.setSortable(true);
        adminColumn.setFieldUpdater(new FieldUpdater<UserOrganismPermissionInfo, Boolean>() {
            @Override
            public void update(int index, UserOrganismPermissionInfo object, Boolean value) {
                object.setAdmin(value);
                UserRestService.updateOrganismPermission(object);
            }
        });
        sortHandler.setComparator(adminColumn, new Comparator<UserOrganismPermissionInfo>() {
            @Override
            public int compare(UserOrganismPermissionInfo o1, UserOrganismPermissionInfo o2) {
                return o1.isAdmin().compareTo(o2.isAdmin());
            }
        });

        organismPermissionsGrid.addColumnSortHandler(sortHandler);
        sortHandler.setComparator(organismNameColumn, new Comparator<UserOrganismPermissionInfo>() {
            @Override
            public int compare(UserOrganismPermissionInfo o1, UserOrganismPermissionInfo o2) {
                return o1.getOrganismName().compareTo(o2.getOrganismName());
            }
        });

        Column<UserOrganismPermissionInfo, Boolean> writeColumn = new Column<UserOrganismPermissionInfo, Boolean>(new CheckboxCell(true, true)) {
            @Override
            public Boolean getValue(UserOrganismPermissionInfo object) {
                return object.isWrite();
            }
        };
        writeColumn.setSortable(true);
        writeColumn.setFieldUpdater(new FieldUpdater<UserOrganismPermissionInfo, Boolean>() {
            @Override
            public void update(int index, UserOrganismPermissionInfo object, Boolean value) {
                object.setWrite(value);
                object.setUserId(selectedUserInfo.getUserId());
                UserRestService.updateOrganismPermission(object);
            }
        });
        sortHandler.setComparator(writeColumn, new Comparator<UserOrganismPermissionInfo>() {
            @Override
            public int compare(UserOrganismPermissionInfo o1, UserOrganismPermissionInfo o2) {
                return o1.isWrite().compareTo(o2.isWrite());
            }
        });

        Column<UserOrganismPermissionInfo, Boolean> exportColumn = new Column<UserOrganismPermissionInfo, Boolean>(new CheckboxCell(true, true)) {
            @Override
            public Boolean getValue(UserOrganismPermissionInfo object) {
                return object.isExport();
            }
        };
        exportColumn.setSortable(true);
        exportColumn.setFieldUpdater(new FieldUpdater<UserOrganismPermissionInfo, Boolean>() {
            @Override
            public void update(int index, UserOrganismPermissionInfo object, Boolean value) {
                object.setExport(value);
                UserRestService.updateOrganismPermission(object);
            }
        });
        sortHandler.setComparator(exportColumn, new Comparator<UserOrganismPermissionInfo>() {
            @Override
            public int compare(UserOrganismPermissionInfo o1, UserOrganismPermissionInfo o2) {
                return o1.isExport().compareTo(o2.isExport());
            }
        });

        Column<UserOrganismPermissionInfo, Boolean> readColumn = new Column<UserOrganismPermissionInfo, Boolean>(new CheckboxCell(true, true)) {
            @Override
            public Boolean getValue(UserOrganismPermissionInfo object) {
                return object.isRead();
            }
        };
        readColumn.setSortable(true);
        readColumn.setFieldUpdater(new FieldUpdater<UserOrganismPermissionInfo, Boolean>() {
            @Override
            public void update(int index, UserOrganismPermissionInfo object, Boolean value) {
                object.setRead(value);
                UserRestService.updateOrganismPermission(object);
            }
        });
        sortHandler.setComparator(readColumn, new Comparator<UserOrganismPermissionInfo>() {
            @Override
            public int compare(UserOrganismPermissionInfo o1, UserOrganismPermissionInfo o2) {
                return o1.isRead().compareTo(o2.isRead());
            }
        });


        organismPermissionsGrid.addColumn(organismNameColumn, "Name");
        organismPermissionsGrid.addColumn(adminColumn, "Admin");
        organismPermissionsGrid.addColumn(writeColumn, "Write");
        organismPermissionsGrid.addColumn(exportColumn, "Export");
        organismPermissionsGrid.addColumn(readColumn, "Read");
        permissionProvider.addDataDisplay(organismPermissionsGrid);

    }


    private void setCurrentUserInfoFromUI() {
        selectedUserInfo.setEmail(email.getText());
        selectedUserInfo.setFirstName(firstName.getText());
        selectedUserInfo.setLastName(lastName.getText());
        selectedUserInfo.setPassword(passwordTextBox.getText());
        selectedUserInfo.setRole(roleList.getSelectedItemText());
    }

    @UiHandler(value = {"firstName", "lastName", "email", "passwordTextBox", "roleList"})
    public void updateName(ChangeEvent changeHandler) {
        // assume an edit operation
        if (selectedUserInfo != null) {
            setCurrentUserInfoFromUI();
            UserRestService.updateUser(userInfoList, selectedUserInfo);
        }
        // if it is to be created then we don't care
    }


    @UiHandler("createButton")
    public void create(ClickEvent clickEvent) {
        selectedUserInfo = null;
        selectionModel.clear();
        updateUserInfo();
        saveButton.setVisible(true);
        cancelButton.setEnabled(true);
        createButton.setEnabled(false);
        passwordRow.setVisible(true);
    }

    @UiHandler("addGroupButton")
    public void addGroupToUser(ClickEvent clickEvent) {
        String selectedGroup = availableGroupList.getSelectedItemText();
        UserRestService.addUserToGroup(selectedGroup, selectedUserInfo);
    }


    @UiHandler("cancelButton")
    public void cancel(ClickEvent clickEvent) {
        updateUserInfo();
        saveButton.setVisible(false);
        cancelButton.setEnabled(false);
        createButton.setEnabled(true);
        passwordRow.setVisible(false);
    }

    @UiHandler("deleteButton")
    public void delete(ClickEvent clickEvent) {
        UserRestService.deleteUser(userInfoList, selectedUserInfo);
        selectedUserInfo = null;
        updateUserInfo();
    }

    @UiHandler("saveButton")
    public void save(ClickEvent clickEvent) {
        selectedUserInfo = new UserInfo();
        setCurrentUserInfoFromUI();
        UserRestService.createUser(userInfoList, selectedUserInfo);
        createButton.setEnabled(true);

        selectedUserInfo = null;
        selectionModel.clear();
        updateUserInfo();
        saveButton.setVisible(false);
        cancelButton.setEnabled(false);
        passwordRow.setVisible(false);
    }

    private void updateUserInfo() {
        passwordTextBox.setText("");
        if (selectedUserInfo == null) {
            firstName.setText("");
            lastName.setText("");
            email.setText("");
            deleteButton.setEnabled(false);
            roleList.setVisible(false);
            permissionProviderList.clear();


            if (saveButton.isVisible()) {
                roleList.setVisible(true);
                UserInfo currentUser = MainPanel.getCurrentUser();
                roleList.setSelectedIndex(0);
                roleList.setEnabled(currentUser.getRole().equalsIgnoreCase("admin"));
            }

        } else {
            firstName.setText(selectedUserInfo.getFirstName());
            lastName.setText(selectedUserInfo.getLastName());
            email.setText(selectedUserInfo.getEmail());
            deleteButton.setEnabled(true);

            UserInfo currentUser = MainPanel.getCurrentUser();

            passwordRow.setVisible(selectedUserInfo.getEmail().equals(currentUser.getEmail()));

            roleList.setVisible(true);

            for (int i = 0; i < roleList.getItemCount(); i++) {
                roleList.setItemSelected(i, selectedUserInfo.getRole().equals(roleList.getItemText(i)));
            }

            // if user is "user" then make uneditable
            // if user is admin AND self then make uneditable
            // if user is admin, but not self, then make editable
            roleList.setEnabled(currentUser.getRole().equalsIgnoreCase("admin") && currentUser.getUserId() != selectedUserInfo.getUserId());

//            groupTable.clear();
            groupTable.removeAllRows();
            List<String> groupList = selectedUserInfo.getGroupList();
            for (int i = 0; i < groupList.size(); i++) {
                String group = groupList.get(i);
                addGroupToUi(group);
            }


            availableGroupList.clear();
            List<String> localAvailableGroupList = selectedUserInfo.getAvailableGroupList();
            for (int i = 0; i < localAvailableGroupList.size(); i++) {
                String availableGroup = localAvailableGroupList.get(i);
                availableGroupList.addItem(availableGroup);
            }

            permissionProviderList.clear();
            permissionProviderList.addAll(selectedUserInfo.getOrganismPermissionMap().values());
        }
    }


    private void addGroupToUi(String group) {
        int i = groupTable.getRowCount();
        groupTable.setWidget(i, 0, new RemoveGroupButton(group));
        groupTable.setWidget(i, 1, new HTML(group));
    }

    public void reload() {
        UserRestService.loadUsers(userInfoList);
        dataGrid.redraw();
    }

    private void removeGroupFromUI(String group) {
        int rowToRemove = -1;
//        Window.alert("row count: "+groupTable.getRowCount());
        for (int row = 0; rowToRemove < 0 && row < groupTable.getRowCount(); ++row) {
//            Window.alert("cell count for row: "+row+ " -> "+groupTable.getCellCount(row));
//            if(groupTable.getCellCount(row)>1){
            RemoveGroupButton removeGroupButton = (RemoveGroupButton) groupTable.getWidget(row, 0);
            if (removeGroupButton.getGroupName().equals(group)) {
                rowToRemove = row;
            }
//            }
        }
        if (rowToRemove >= 0) {
            groupTable.removeRow(rowToRemove);
            availableGroupList.addItem(group);
        }
    }

    private class RemoveGroupButton extends org.gwtbootstrap3.client.ui.Button {

        private String groupName;

        public RemoveGroupButton(final String groupName) {
            this.groupName = groupName;
            setIcon(IconType.REMOVE);
//            setText("X");
            addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    UserRestService.removeUserFromGroup(groupName, userInfoList, selectedUserInfo);
                }
            });
        }

        public String getGroupName() {
            return groupName;
        }
    }

}
package org.bbop.apollo.gwt.client.rest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.*;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import org.bbop.apollo.gwt.client.Annotator;
import org.bbop.apollo.gwt.client.dto.UserInfo;
import org.bbop.apollo.gwt.client.dto.UserOrganismPermissionInfo;
import org.bbop.apollo.gwt.client.event.UserChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ndunn on 1/14/15.
 */
public class UserRestService {


    public static void login(RequestCallback requestCallback, JSONObject data) {
        RestService.sendRequest(requestCallback, "/Login", data.toString());
    }

    public static void login(String username, String password, Boolean rememberMe) {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                Window.Location.reload();
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("Error loading organisms");
            }
        };
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("operation", new JSONString("login"));
        jsonObject.put("username", new JSONString(username));
        jsonObject.put("password", new JSONString(password));
        jsonObject.put("rememberMe", JSONBoolean.getInstance(rememberMe));
        login(requestCallback, jsonObject);
    }

    public static void loadUsers(RequestCallback requestCallback) {
        RestService.sendRequest(requestCallback, "/user/loadUsers/");
    }

    public static void loadUsers(final List<UserInfo> userInfoList) {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                JSONValue returnValue = JSONParser.parseStrict(response.getText());
                JSONArray array = returnValue.isArray();

                userInfoList.clear();

                for (int i = 0; i < array.size(); i++) {
                    JSONObject object = array.get(i).isObject();

                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserId((long) object.get("userId").isNumber().doubleValue());
                    userInfo.setFirstName(object.get("firstName").isString().stringValue());
                    userInfo.setLastName(object.get("lastName").isString().stringValue());
                    userInfo.setEmail(object.get("username").isString().stringValue());
                    if (object.get("role") != null && object.get("role").isString() != null) {
                        userInfo.setRole(object.get("role").isString().stringValue().toLowerCase());
                    } else {
                        userInfo.setRole("user");
                    }

                    JSONArray groupArray = object.get("groups").isArray();
                    List<String> groupList = new ArrayList<>();
                    for (int j = 0; j < groupArray.size(); j++) {
                        String groupValue = groupArray.get(j).isObject().get("name").isString().stringValue();
                        groupList.add(groupValue);
                    }
                    userInfo.setGroupList(groupList);

                    JSONArray availableGroupArray = object.get("availableGroups").isArray();
                    List<String> availableGroupList = new ArrayList<>();
                    for (int j = 0; j < availableGroupArray.size(); j++) {
                        String availableGroupValue = availableGroupArray.get(j).isObject().get("name").isString().stringValue();
                        availableGroupList.add(availableGroupValue);
                    }
                    userInfo.setAvailableGroupList(availableGroupList);


                    // TODO: use shared permission enums
                    JSONArray organismArray = object.get("organismPermissions").isArray();
                    Map<String, UserOrganismPermissionInfo> organismPermissionMap = new TreeMap<>();
                    for (int j = 0; j < organismArray.size(); j++) {
                        JSONObject organismPermissionJsonObject = organismArray.get(j).isObject();
                        UserOrganismPermissionInfo userOrganismPermissionInfo = new UserOrganismPermissionInfo();
                        if(organismPermissionJsonObject.get("id")!=null){
                            userOrganismPermissionInfo.setId((long) organismPermissionJsonObject.get("id").isNumber().doubleValue());
                        }
                        userOrganismPermissionInfo.setUserId((long) organismPermissionJsonObject.get("userId").isNumber().doubleValue());
                        userOrganismPermissionInfo.setOrganismName(organismPermissionJsonObject.get("organism").isString().stringValue());
                        if(organismPermissionJsonObject.get("permissions")!=null) {
                            JSONArray permissionsArray = JSONParser.parseStrict(organismPermissionJsonObject.get("permissions").isString().stringValue()).isArray();
                            for (int permissionIndex = 0; permissionIndex < permissionsArray.size(); ++permissionIndex) {
                                String permission = permissionsArray.get(permissionIndex).isString().stringValue();
                                switch (permission) {
                                    case "ADMINISTRATE":
                                        userOrganismPermissionInfo.setAdmin(true);
                                        break;
                                    case "WRITE":
                                        userOrganismPermissionInfo.setWrite(true);
                                        break;
                                    case "EXPORT":
                                        userOrganismPermissionInfo.setExport(true);
                                        break;
                                    case "READ":
                                        userOrganismPermissionInfo.setRead(true);
                                        break;

                                    default:
                                        Window.alert("not sure what to do wtih this: " + permission);
                                }
                            }
                        }


                        organismPermissionMap.put(userOrganismPermissionInfo.getOrganismName(), userOrganismPermissionInfo);
                    }
                    userInfo.setOrganismPermissionMap(organismPermissionMap);

                    userInfoList.add(userInfo);
                }

            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("Error loading organisms");
            }
        };

        loadUsers(requestCallback);
    }

    public static void logout(RequestCallback requestCallback) {
        RestService.sendRequest(requestCallback, "/Login?operation=logout");
    }

    public static void logout() {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                Window.Location.reload();
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("Error logging out " + exception);
            }
        };
        logout(requestCallback);
    }

    public static void updateUser(final List<UserInfo> userInfoList, UserInfo selectedUserInfo) {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                loadUsers(userInfoList);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("Error updating user: " + exception);
            }
        };
        JSONObject jsonObject = selectedUserInfo.toJSON();
        RestService.sendRequest(requestCallback, "/user/updateUser", "data=" + jsonObject.toString());
    }

    public static void deleteUser(final List<UserInfo> userInfoList, UserInfo selectedUserInfo) {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                loadUsers(userInfoList);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("Error deleting user: " + exception);
            }
        };
        JSONObject jsonObject = selectedUserInfo.toJSON();
        RestService.sendRequest(requestCallback, "/user/deleteUser", "data=" + jsonObject.toString());
    }

    public static void createUser(final List<UserInfo> userInfoList, UserInfo selectedUserInfo) {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                loadUsers(userInfoList);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("Error adding user: " + exception);
            }
        };
        JSONObject jsonObject = selectedUserInfo.toJSON();
        RestService.sendRequest(requestCallback, "/user/createUser", "data=" + jsonObject.toString());

    }

    public static void removeUserFromGroup(final String groupName, final List<UserInfo> userInfoList, final UserInfo selectedUserInfo) {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                List<UserInfo> userInfoList = new ArrayList<>();
                userInfoList.add(selectedUserInfo);
                Annotator.eventBus.fireEvent(new UserChangeEvent(userInfoList, UserChangeEvent.Action.RELOAD_USERS));
                Annotator.eventBus.fireEvent(new UserChangeEvent(userInfoList, UserChangeEvent.Action.REMOVE_USER_FROM_GROUP, groupName));
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("Error removing group from user: " + exception);
            }
        };
        JSONObject jsonObject = selectedUserInfo.toJSON();
        jsonObject.put("group", new JSONString(groupName));
        RestService.sendRequest(requestCallback, "/user/removeUserFromGroup", "data=" + jsonObject.toString());
    }

    public static void addUserToGroup(final String groupName, final UserInfo selectedUserInfo) {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                List<UserInfo> userInfoList = new ArrayList<>();
                userInfoList.add(selectedUserInfo);
                Annotator.eventBus.fireEvent(new UserChangeEvent(userInfoList, UserChangeEvent.Action.RELOAD_USERS));
                Annotator.eventBus.fireEvent(new UserChangeEvent(userInfoList, UserChangeEvent.Action.ADD_USER_TO_GROUP, groupName));
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("Error adding group to user: " + exception);
            }
        };
        JSONObject jsonObject = selectedUserInfo.toJSON();
        jsonObject.put("group", new JSONString(groupName));
        RestService.sendRequest(requestCallback, "/user/addUserToGroup", "data=" + jsonObject.toString());
    }

    public static void updateOrganismPermission(UserOrganismPermissionInfo object) {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                GWT.log("success");
//                loadUsers(userInfoList);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("Error updating permissions: " + exception);
            }
        };
        RestService.sendRequest(requestCallback, "/user/updateOrganismPermission", "data=" + object.toJSON());
    }
}

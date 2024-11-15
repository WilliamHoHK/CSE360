// UserManagementController.java
package simpleDatabase;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import Encryption.EncryptionHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class UserManagementController {

    // Simulated databases
    protected static Map<String, UserAccount> userDB = new HashMap<>();
    protected static Map<String, Invite> inviteDB = new HashMap<>();
    protected static Map<String, TempPass> tempPassDB = new HashMap<>();
    protected static Map<String, Set<String>> specialAccessGroups = new HashMap<>(); // Map for special access groups

    // Role constants
    protected static final String ROLE_ADMINISTRATOR = "Administrator";
    protected static final String ROLE_LEARNER = "Student";
    protected static final String ROLE_TEACHER = "Teacher";

    // Variables to keep track of the currently logged-in user's username
    protected String loggedInAdmin;

    // Encryption helper
    protected EncryptionHelper encryptionHelper;

    // Set the encryption helper
    public void setEncryptionHelper(EncryptionHelper helper) {
        this.encryptionHelper = helper;
    }

    // Admin Home Screen additions for managing Special Access Groups
    // Admin Home Screen
public void displayHomeScreen(Stage window, UserAccount user, String role) {
    VBox homeLayout = new VBox(10);
    homeLayout.setPadding(new Insets(10));

    String displayName = user.getPreferredName() != null ? user.getPreferredName() : user.getFirstName();
    Label welcomeLbl = new Label("Welcome, " + displayName + " (" + role + ")!");
    Button logoutBtn = new Button("Logout");

    homeLayout.getChildren().addAll(welcomeLbl, logoutBtn);

    if (ROLE_ADMINISTRATOR.equals(role)) {
        loggedInAdmin = user.getUsername();

        // Buttons for admin actions
        Button inviteBtn = new Button("Invite User");
        inviteBtn.setOnAction(e -> openInviteUserScreen(window));

        Button resetBtn = new Button("Reset User Account");
        resetBtn.setOnAction(e -> openResetUserScreen(window));

        Button deleteBtn = new Button("Delete User Account");
        deleteBtn.setOnAction(e -> openDeleteUserScreen(window));

        Button listBtn = new Button("List Users");
        listBtn.setOnAction(e -> openListUsersScreen(window));

        Button modifyBtn = new Button("Modify User Roles");
        modifyBtn.setOnAction(e -> openModifyRolesScreen(window));

        // NEWLY ADDED: Manage Special Access Groups Button
        Button manageGroupsBtn = new Button("Manage Special Access Groups");
        manageGroupsBtn.setOnAction(e -> openManageSpecialAccessGroupsScreen(window));
        // NEWLY ADDED END

        // Article management buttons
        ArticleManagementController articleController = new ArticleManagementController();
        articleController.setEncryptionHelper(encryptionHelper);

        Button createArticleBtn = new Button("Create Help Article");
        createArticleBtn.setOnAction(e -> articleController.openCreateArticleScreen(window, user));

        Button manageArticlesBtn = new Button("Manage Help Articles");
        manageArticlesBtn.setOnAction(e -> articleController.openManageArticlesScreen(window, user));

        Button backupBtn = new Button("Backup Articles");
        backupBtn.setOnAction(e -> articleController.openBackupArticlesScreen(window, user));

        Button restoreBtn = new Button("Restore Articles");
        restoreBtn.setOnAction(e -> articleController.openRestoreArticlesScreen(window, user));

        // Add all admin buttons to the layout
        homeLayout.getChildren().addAll(
            inviteBtn, resetBtn, deleteBtn, listBtn, modifyBtn, manageGroupsBtn, 
            createArticleBtn, manageArticlesBtn, backupBtn, restoreBtn
        );

        window.setTitle("Admin Home");
    } else if (ROLE_TEACHER.equals(role)) {
        // Teacher Home - Article management buttons
        ArticleManagementController articleController = new ArticleManagementController();
        articleController.setEncryptionHelper(encryptionHelper);

        Button createArticleBtn = new Button("Create Help Article");
        createArticleBtn.setOnAction(e -> articleController.openCreateArticleScreen(window, user));

        Button manageArticlesBtn = new Button("Manage Help Articles");
        manageArticlesBtn.setOnAction(e -> articleController.openManageArticlesScreen(window, user));

        Button backupBtn = new Button("Backup Articles");
        backupBtn.setOnAction(e -> articleController.openBackupArticlesScreen(window, user));

        Button restoreBtn = new Button("Restore Articles");
        restoreBtn.setOnAction(e -> articleController.openRestoreArticlesScreen(window, user));

        // Add teacher-specific buttons to the layout
        homeLayout.getChildren().addAll(createArticleBtn, manageArticlesBtn, backupBtn, restoreBtn);

        window.setTitle("Instructor Home");
    } else {
        // Student or Learner Home
        window.setTitle("User Home");
        ArticleManagementController articleController = new ArticleManagementController();
        articleController.setEncryptionHelper(encryptionHelper);

        Button searchArticlesBtn = new Button("Search Help Articles");
        searchArticlesBtn.setOnAction(e -> articleController.openSearchArticlesScreen(window, user));

        // Add search button for student/learner
        homeLayout.getChildren().add(searchArticlesBtn);
    }

    logoutBtn.setOnAction(e -> {
        loggedInAdmin = null;
        window.setScene(new Scene(buildLoginScreen(window), 400, 350));
    });

    Scene homeScene = new Scene(homeLayout, 400, 500);
    window.setScene(homeScene);
}


    // Screen to manage Special Access Groups
    public void openManageSpecialAccessGroupsScreen(Stage window) {
        VBox manageGroupsLayout = new VBox(10);
        manageGroupsLayout.setPadding(new Insets(10));

        Label titleLbl = new Label("Special Access Groups Management:");
        manageGroupsLayout.getChildren().add(titleLbl);

        // List existing groups
        for (String groupName : specialAccessGroups.keySet()) {
            Label groupLbl = new Label("Group Name: " + groupName);
            Button viewGroupBtn = new Button("View Group");
            Button deleteGroupBtn = new Button("Delete Group");

            HBox groupBox = new HBox(5, groupLbl, viewGroupBtn, deleteGroupBtn);
            manageGroupsLayout.getChildren().add(groupBox);

            viewGroupBtn.setOnAction(e -> openViewSpecialAccessGroupScreen(window, groupName));
            deleteGroupBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Special Access Group");
                alert.setHeaderText("Delete group: " + groupName + "?");
                alert.setContentText("This cannot be undone.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    specialAccessGroups.remove(groupName);
                    manageGroupsLayout.getChildren().remove(groupBox);
                }
            });
        }

        // Add New Special Access Group
        Label newGroupLbl = new Label("New Group Name:");
        TextField newGroupTxt = new TextField();
        Button createGroupBtn = new Button("Create Group");

        createGroupBtn.setOnAction(e -> {
            String groupName = newGroupTxt.getText();
            if (groupName.isEmpty() || specialAccessGroups.containsKey(groupName)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid Group Name");
                alert.setContentText("Please provide a valid, unique group name.");
                alert.showAndWait();
            } else {
                specialAccessGroups.put(groupName, new HashSet<>());
                manageGroupsLayout.getChildren().add(new Label("Group Created: " + groupName));
            }
        });

        manageGroupsLayout.getChildren().addAll(newGroupLbl, newGroupTxt, createGroupBtn);

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        manageGroupsLayout.getChildren().add(backBtn);

        Scene manageGroupsScene = new Scene(manageGroupsLayout, 500, 600);
        window.setScene(manageGroupsScene);
    }

    // View Special Access Group Screen
    public void openViewSpecialAccessGroupScreen(Stage window, String groupName) {
        VBox viewGroupLayout = new VBox(10);
        viewGroupLayout.setPadding(new Insets(10));

        Label titleLbl = new Label("Group: " + groupName);
        viewGroupLayout.getChildren().add(titleLbl);

        Set<String> groupMembers = specialAccessGroups.get(groupName);
        if (groupMembers != null && !groupMembers.isEmpty()) {
            for (String member : groupMembers) {
                Label memberLbl = new Label("Member: " + member);
                Button removeMemberBtn = new Button("Remove");

                HBox memberBox = new HBox(5, memberLbl, removeMemberBtn);
                viewGroupLayout.getChildren().add(memberBox);

                removeMemberBtn.setOnAction(e -> {
                    groupMembers.remove(member);
                    viewGroupLayout.getChildren().remove(memberBox);
                });
            }
        } else {
            viewGroupLayout.getChildren().add(new Label("No members found."));
        }

        // Add member to the group
        Label newMemberLbl = new Label("Add New Member (username):");
        TextField newMemberTxt = new TextField();
        Button addMemberBtn = new Button("Add Member");

        addMemberBtn.setOnAction(e -> {
            String newMember = newMemberTxt.getText();
            if (!userDB.containsKey(newMember)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("User Not Found");
                alert.setContentText("Please enter a valid username.");
                alert.showAndWait();
            } else {
                groupMembers.add(newMember);
                viewGroupLayout.getChildren().add(new Label("Member Added: " + newMember));
            }
        });

        viewGroupLayout.getChildren().addAll(newMemberLbl, newMemberTxt, addMemberBtn);

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> openManageSpecialAccessGroupsScreen(window));
        viewGroupLayout.getChildren().add(backBtn);

        Scene viewGroupScene = new Scene(viewGroupLayout, 500, 600);
        window.setScene(viewGroupScene);
    }
}



protected static Map<String, Map<Long, CreateArticle>> specialArticleDB = new HashMap<>(); // NEWLY ADDED: Special access groups and articles


// Open Create Article Screen
public void openCreateArticleScreen(Stage window, UserAccount user) {
    GridPane createGrid = new GridPane();
    createGrid.setPadding(new Insets(10));
    createGrid.setVgap(8);
    createGrid.setHgap(10);

    Label levelLbl = new Label("Level:");
    ComboBox<String> levelCombo = new ComboBox<>();
    levelCombo.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert");
    levelCombo.setValue("Intermediate");

    Label groupsLbl = new Label("Groups (comma-separated):");
    TextField groupsTxt = new TextField();

    // NEWLY ADDED: Allow choosing between General and Special Group
    Label specialGroupLbl = new Label("Special Access Group (Optional):");
    ComboBox<String> specialGroupCombo = new ComboBox<>();
    specialGroupCombo.getItems().addAll(specialAccessGroups.keySet());
    specialGroupCombo.setValue(null); // Default to no special group
    // NEWLY ADDED END

    Label titleLbl = new Label("Title:");
    TextField titleTxt = new TextField();

    Label descLbl = new Label("Short Description:");
    TextField descTxt = new TextField();

    Label keywordsLbl = new Label("Keywords (comma-separated):");
    TextField keywordsTxt = new TextField();

    Label bodyLbl = new Label("Body:");
    TextArea bodyTxt = new TextArea();

    Label linksLbl = new Label("Links (comma-separated):");
    TextField linksTxt = new TextField();

    Button saveBtn = new Button("Save Article");
    Button backBtn = new Button("Back");
    Label statusLbl = new Label();

    createGrid.add(levelLbl, 0, 0);
    createGrid.add(levelCombo, 1, 0);
    createGrid.add(groupsLbl, 0, 1);
    createGrid.add(groupsTxt, 1, 1);
    createGrid.add(specialGroupLbl, 0, 2); // NEWLY ADDED
    createGrid.add(specialGroupCombo, 1, 2); // NEWLY ADDED
    createGrid.add(titleLbl, 0, 3);
    createGrid.add(titleTxt, 1, 3);
    createGrid.add(descLbl, 0, 4);
    createGrid.add(descTxt, 1, 4);
    createGrid.add(keywordsLbl, 0, 5);
    createGrid.add(keywordsTxt, 1, 5);
    createGrid.add(bodyLbl, 0, 6);
    createGrid.add(bodyTxt, 1, 6);
    createGrid.add(linksLbl, 0, 7);
    createGrid.add(linksTxt, 1, 7);
    createGrid.add(saveBtn, 1, 8);
    createGrid.add(backBtn, 0, 8);
    createGrid.add(statusLbl, 1, 9);

    window.setTitle("Create Help Article");

    saveBtn.setOnAction(e -> {
        String level = levelCombo.getValue();
        Set<String> groups = new HashSet<>(Arrays.asList(groupsTxt.getText().split(",")));
        String title = titleTxt.getText();
        String shortDesc = descTxt.getText();
        Set<String> keywords = new HashSet<>(Arrays.asList(keywordsTxt.getText().split(",")));
        String body = bodyTxt.getText();
        List<String> links = Arrays.asList(linksTxt.getText().split(","));

        if (title.isEmpty() || body.isEmpty()) {
            statusLbl.setText("Title and Body are required.");
            return;
        }

        long id = generateArticleId();
        CreateArticle article = new CreateArticle(id, level, groups, title, shortDesc, keywords, body, links);

        // NEWLY ADDED: Save to Special Group if Selected
        String specialGroup = specialGroupCombo.getValue();
        if (specialGroup != null && !specialGroup.isEmpty()) {
            specialArticleDB.putIfAbsent(specialGroup, new HashMap<>());
            specialArticleDB.get(specialGroup).put(id, article);
            statusLbl.setText("Article saved to Special Access Group: " + specialGroup);
        } else {
            articleDB.put(id, article);
            statusLbl.setText("Article saved with ID: " + id);
        }
        // NEWLY ADDED END
    });

    backBtn.setOnAction(e -> {
        if (user.getRoles().contains(UserManagementController.ROLE_ADMINISTRATOR)) {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);
            userController.displayHomeScreen(window, user, UserManagementController.ROLE_ADMINISTRATOR);
        } else {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);
            userController.displayHomeScreen(window, user, UserManagementController.ROLE_TEACHER);
        }
    });

    Scene createScene = new Scene(createGrid, 600, 600);
    window.setScene(createScene);
}



// NEWLY ADDED: Open View Special Group Articles Screen
public void openViewSpecialGroupArticlesScreen(Stage window, UserAccount user, String specialGroup) {
    if (!specialArticleDB.containsKey(specialGroup)) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Group Not Found");
        alert.setContentText("The specified special access group does not exist.");
        alert.showAndWait();
        return;
    }

    VBox viewLayout = new VBox(10);
    viewLayout.setPadding(new Insets(10));

    Label titleLbl = new Label("Articles in Special Group: " + specialGroup);
    viewLayout.getChildren().add(titleLbl);

    Map<Long, CreateArticle> articles = specialArticleDB.get(specialGroup);
    if (articles.isEmpty()) {
        viewLayout.getChildren().add(new Label("No articles found in this group."));
    } else {
        for (CreateArticle article : articles.values()) {
            Label articleLbl = new Label("ID: " + article.getId() + ", Title: " + article.getTitle());
            Button viewBtn = new Button("View");
            Button editBtn = new Button("Edit");
            Button deleteBtn = new Button("Delete");

            HBox articleBox = new HBox(5, articleLbl, viewBtn, editBtn, deleteBtn);
            viewLayout.getChildren().add(articleBox);

            viewBtn.setOnAction(e -> openViewArticleScreen(window, user, article));
            editBtn.setOnAction(e -> openEditArticleScreen(window, user, article));
            deleteBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Article");
                alert.setHeaderText("Delete article: " + article.getTitle() + "?");
                alert.setContentText("This cannot be undone.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    articles.remove(article.getId());
                    viewLayout.getChildren().remove(articleBox);
                }
            });
        }
    }

    Button backBtn = new Button("Back");
    backBtn.setOnAction(e -> {
        UserManagementController userController = new UserManagementController();
        userController.setEncryptionHelper(encryptionHelper);
        userController.displayHomeScreen(window, user, UserManagementController.ROLE_ADMINISTRATOR);
    });
    viewLayout.getChildren().add(backBtn);

    Scene viewScene = new Scene(viewLayout, 600, 600);
    window.setScene(viewScene);
}



// Backup Articles Method
protected void backupArticles(String filename, Set<String> groups, String specialGroup) throws Exception {
    List<CreateArticle> articlesToBackup = new ArrayList<>();
    if (specialGroup != null && !specialGroup.isEmpty() && specialArticleDB.containsKey(specialGroup)) {
        articlesToBackup.addAll(specialArticleDB.get(specialGroup).values());
    } else {
        if (groups.isEmpty() || groups.contains("")) {
            articlesToBackup.addAll(articleDB.values());
        } else {
            for (CreateArticle article : articleDB.values()) {
                for (String group : article.getGroups()) {
                    if (groups.contains(group.trim())) {
                        articlesToBackup.add(article);
                        break;
                    }
                }
            }
        }
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(articlesToBackup);
    oos.close();

    byte[] plainData = baos.toByteArray();
    byte[] iv = EncryptionUtils.getInitializationVector(filename.toCharArray());
    byte[] encryptedData = encryptionHelper.encrypt(plainData, iv);

    FileOutputStream fos = new FileOutputStream(filename);
    fos.write(encryptedData);
    fos.close();
}


// Restore Articles Method
protected void restoreArticles(String filename, boolean removeExisting, String specialGroup) {
    try {
        FileInputStream fis = new FileInputStream(filename);
        byte[] encryptedData = fis.readAllBytes();
        fis.close();

        byte[] iv = EncryptionUtils.getInitializationVector(filename.toCharArray());
        byte[] plainData = encryptionHelper.decrypt(encryptedData, iv);

        ByteArrayInputStream bais = new ByteArrayInputStream(plainData);
        ObjectInputStream ois = new ObjectInputStream(bais);

        @SuppressWarnings("unchecked")
        List<CreateArticle> restoredArticles = (List<CreateArticle>) ois.readObject();
        ois.close();

        if (removeExisting) {
            if (specialGroup != null && !specialGroup.isEmpty()) {
                specialArticleDB.put(specialGroup, new HashMap<>());
            } else {
                articleDB.clear();
                nextArticleId = 1;
            }
        }

        for (CreateArticle article : restoredArticles) {
            if (article.getId() >= nextArticleId) {
                nextArticleId = article.getId() + 1;
            }
            if (specialGroup != null && !specialGroup.isEmpty()) {
                specialArticleDB.get(specialGroup).put(article.getId(), article);
            } else {
                articleDB.put(article.getId(), article);
            }
        }

        // Log the number of articles restored
        System.out.println("Number of articles restored: " + restoredArticles.size());
        System.out.println("Article IDs restored: " + (specialGroup != null ? specialArticleDB.get(specialGroup).keySet() : articleDB.keySet()));

    } catch (Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Restore Failed");
        alert.setHeaderText("An error occurred during restore.");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}

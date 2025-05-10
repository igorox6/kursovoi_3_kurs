package org.example.kursovoi.controllers;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.db_classes.*;
import org.example.kursovoi.interfases.InitializableController;

import java.net.URL;
import java.net.http.HttpClient;
import java.util.*;
import java.util.stream.Collectors;

public class SellerController implements InitializableController {

    @FXML
    private TreeView<String> catalogTreeView;

    @FXML
    private TextField minPriceField;

    @FXML
    private TextField maxPriceField;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private VBox brandsBox;

    @FXML
    private ImageView iconOrders;

    @FXML
    private ImageView iconBasket;

    @FXML
    private ScrollPane productsScrollPane;

    @FXML
    private HBox categoryFilterBox;

    @FXML
    private Label selectedCategoryLabel;

    @FXML
    private Button ordersButton;

    @FXML
    private Button basketButton;

    private Application app;
    private UserSession userSession;
    private HttpClient httpClient;
    private Gson gson;

    private List<Product> allProducts;
    private List<CheckBox> brandCheckBoxes = new ArrayList<>();
    private Category selectedCategory;
    private List<Category> allCategories;
    private Map<Long, String> companyNames;

    @Override
    public void setMainApp(Application app, UserSession userSession) {
        this.app = app;
        this.userSession = UserSession.getInstance();
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        if (userSession.getToken() != null) {
            Category.initializeCategories(userSession.getToken());
            Product.initializeProducts(userSession.getToken());
            Company.initializeCompanies(userSession.getToken());
            Order.initializeAllOrders(userSession.getToken());
            Buyer.initializeBuyersFromList(userSession.getToken());
            loadData();
        } else {
            System.out.println("Error: Token is null, cannot initialize data.");
        }
    }

    @Override
    public void setMainAppWithObject(Application app, UserSession userSession, Object data) {
        this.app = app;
        this.userSession = UserSession.getInstance();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadIcons();

        catalogTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String categoryName = newVal.getValue();
                if ("Каталог".equals(categoryName)) {
                    selectedCategory = null;
                    categoryFilterBox.setVisible(false);
                    categoryFilterBox.setManaged(false);
                } else {
                    selectedCategory = getCategoryByName(categoryName);
                    if (selectedCategory != null) {
                        selectedCategoryLabel.setText(categoryName);
                        categoryFilterBox.setVisible(true);
                        categoryFilterBox.setManaged(true);
                    }
                }
                filterAndSortProducts();
            }
        });
        minPriceField.textProperty().addListener((obs, oldVal, newVal) -> filterAndSortProducts());
        maxPriceField.textProperty().addListener((obs, oldVal, newVal) -> filterAndSortProducts());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndSortProducts());
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> filterAndSortProducts());
    }

    private void loadIcons() {
        iconOrders.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/package.png")).toExternalForm()));
        iconBasket.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/shopping-basket.png")).toExternalForm()));
    }

    private void loadData() {
        if (userSession == null || userSession.getToken() == null) {
            System.out.println("Error: UserSession or token is null. Cannot load data.");
            return;
        }
        allCategories = Category.getCategories();
        allProducts = Product.getProducts();
        companyNames = Company.getCompanies().stream()
                .collect(Collectors.toMap(Company::getId, Company::getName));
        initializeCategories();
        loadBrands();
        filterAndSortProducts();
    }

    private void initializeCategories() {
        TreeItem<String> rootItem = new TreeItem<>("Каталог");
        rootItem.setExpanded(true);

        Map<Long, TreeItem<String>> categoryMap = new HashMap<>();
        categoryMap.put(null, rootItem);

        if (allCategories == null) {
            System.out.println("Error: allCategories is null, cannot initialize categories.");
            return;
        }

        for (Category category : allCategories) {
            if (category == null || category.getName() == null) {
                System.out.println("Warning: Skipping null category or category with null name.");
                continue;
            }
            TreeItem<String> item = new TreeItem<>(category.getName());
            categoryMap.put(category.getId(), item);
            if (category.getId_parent_category() != null) {
                TreeItem<String> parent = categoryMap.get(category.getId_parent_category());
                if (parent != null) {
                    parent.getChildren().add(item);
                }
            } else {
                rootItem.getChildren().add(item);
            }
        }

        catalogTreeView.setRoot(rootItem);
        catalogTreeView.setShowRoot(true);
    }

    private Category getCategoryByName(String name) {
        if (allCategories == null) return null;
        return allCategories.stream()
                .filter(c -> c != null && name.equals(c.getName()))
                .findFirst()
                .orElse(null);
    }

    private void loadBrands() {
        if (allProducts == null || companyNames == null) {
            System.out.println("Error: allProducts or companyNames is null, cannot load brands.");
            return;
        }

        Set<String> brands = allProducts.stream()
                .map(product -> companyNames.get(product.getIdCompany()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        brandsBox.getChildren().clear();
        brandCheckBoxes.clear();

        for (String brand : brands) {
            CheckBox checkBox = new CheckBox(brand);
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> filterAndSortProducts());
            brandsBox.getChildren().add(checkBox);
            brandCheckBoxes.add(checkBox);
        }
    }

    private void filterAndSortProducts() {
        if (allProducts == null) {
            System.out.println("Error: allProducts is null, cannot filter products.");
            return;
        }

        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        List<String> selectedBrands = brandCheckBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());

        List<Product> filteredProducts = allProducts.stream()
                .filter(product -> {
                    if (product == null || product.getProductName() == null) return false;
                    if (!searchText.isEmpty() && !product.getProductName().toLowerCase().contains(searchText)) {
                        return false;
                    }
                    if (!selectedBrands.isEmpty()) {
                        String companyName = companyNames.get(product.getIdCompany());
                        if (companyName == null || !selectedBrands.contains(companyName)) {
                            return false;
                        }
                    }
                    try {
                        long minPrice = minPriceField.getText().isEmpty() ? 0 : Long.parseLong(minPriceField.getText());
                        long maxPrice = maxPriceField.getText().isEmpty() ? Long.MAX_VALUE : Long.parseLong(maxPriceField.getText());
                        long productCost = product.getCost();
                        if (productCost < minPrice || productCost > maxPrice) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore invalid price input
                    }
                    if (selectedCategory != null) {
                        return isProductInCategoryHierarchy(product, selectedCategory);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        String sortOption = sortComboBox.getSelectionModel().getSelectedItem();
        if (sortOption != null) {
            switch (sortOption) {
                case "А-Я":
                    filteredProducts.sort(Comparator.comparing(Product::getProductName));
                    break;
                case "Я-А":
                    filteredProducts.sort(Comparator.comparing(Product::getProductName).reversed());
                    break;
                case "Цена по возрастанию":
                    filteredProducts.sort(Comparator.comparingLong(Product::getCost));
                    break;
                case "Цена по убыванию":
                    filteredProducts.sort(Comparator.comparingLong(Product::getCost).reversed());
                    break;
                default:
                    break;
            }
        }

        VBox productContainer = new VBox(10);
        productContainer.setPadding(new javafx.geometry.Insets(10));

        for (Product product : filteredProducts) {
            if (product == null) continue;
            String companyName = companyNames.getOrDefault(product.getIdCompany(), "Unknown");
            Label productLabel = new Label(product.getProductName() + " - " + product.getCost() + " руб. (" + companyName + ")");
            productLabel.setOnMouseClicked(event -> openProductDetails(product));
            productLabel.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-cursor: hand;");
            productContainer.getChildren().add(productLabel);
        }

        productsScrollPane.setContent(productContainer);
    }

    private boolean isProductInCategoryHierarchy(Product product, Category selectedCategory) {
        List<Category> productCategories = product.getCategories();
        if (productCategories == null || productCategories.isEmpty()) return false;

        Set<Long> allSubCategoryIds = new HashSet<>();
        collectSubCategoryIds(selectedCategory, allSubCategoryIds);

        return productCategories.stream()
                .anyMatch(c -> c != null && allSubCategoryIds.contains(c.getId()));
    }

    private void collectSubCategoryIds(Category category, Set<Long> subCategoryIds) {
        if (category == null) return;
        subCategoryIds.add(category.getId());
        List<Category> children = allCategories.stream()
                .filter(c -> c != null && Objects.equals(c.getId_parent_category(), category.getId()))
                .collect(Collectors.toList());
        for (Category child : children) {
            collectSubCategoryIds(child, subCategoryIds);
        }
    }

    private void openProductDetails(Product product) {
        app.switchSceneWithObject("/org/example/kursovoi/product-detailed-seller-view.fxml", product);
    }

    @FXML
    public void clearCategoryFilter(ActionEvent event) {
        selectedCategory = null;
        categoryFilterBox.setVisible(false);
        categoryFilterBox.setManaged(false);
        filterAndSortProducts();
    }

    @FXML
    public void onOrdersClick(ActionEvent actionEvent) {
        app.switchScene("/org/example/kursovoi/orders-seller-view.fxml");
    }

    @FXML
    public void onBasketClick(ActionEvent actionEvent) {
        System.out.println("Переход на вкладку заказа продавца");
    }
}
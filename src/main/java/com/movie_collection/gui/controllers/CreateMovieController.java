package com.movie_collection.gui.controllers;

import com.google.inject.Inject;
import com.movie_collection.be.Category;
import com.movie_collection.be.Category2;
import com.movie_collection.be.Movie;
import com.movie_collection.be.Movie2;
import com.movie_collection.bll.utilities.AlertHelper;
import com.movie_collection.gui.controllers.abstractController.RootController;
import com.movie_collection.gui.models.ICategoryModel;
import com.movie_collection.gui.models.IMovieModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateMovieController extends RootController implements Initializable {

    @FXML
    private Label labelMovieWindow;
    @FXML
    private Spinner<Double> personalRatingSpin;
    @FXML
    private TextField movieName,path,durationField;
    @FXML
    public Button onClickSelectFile,confirm_action,cancelOnAction;
    @FXML
    private MenuButton categoryMenuButton;
    private final IMovieModel movieModel;
    private final ICategoryModel categoryModel;
    private final MovieController movieController;

    private Movie editableMovie;
    private boolean isEditable = false;

    @Inject
    public CreateMovieController(IMovieModel movieModel, ICategoryModel categoryModel, MovieController controller) {
        this.movieModel = movieModel;
        this.categoryModel = categoryModel;
        this.movieController = controller;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fillCategorySelection();
        setComponentRules();
        onClickSelectFile.setOnAction(this::selectFileChooser);
        cancelOnAction.setOnAction(e -> getStage().close());
    }

    /**
     * method that takes care of setting the file chooser to be active
     * @param actionEvent triggered event
     */
    private void selectFileChooser(ActionEvent actionEvent) {
        var chooseFile = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SoundFiles files (*.mp4,*.mpeg4)", "*.mp4", "*.mpeg4");
        chooseFile.getExtensionFilters().add(extFilter);

        File selectedMovieFile = chooseFile.showOpenDialog(new Stage());
        if (selectedMovieFile != null) {
            path.setText(selectedMovieFile.toURI().toString());
            var media = new Media(selectedMovieFile.toURI().toString().replace("\\", "/"));
            var duration = (int) media.getDuration().toSeconds();
            durationField.setText(duration / 60 + ":" + duration % 60);
            if (movieName.getText().isBlank())
                movieName.setText(selectedMovieFile.getName());
        }
    }

    /**
     * takes care of the value factory for spinner once instantiated
     */
    private void setComponentRules() {
        SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 10.0,1.0, 0.5);
        personalRatingSpin.setValueFactory(valueFactory);
    }

    /**
     * fill all the categories from all available categories
     */
    private void fillCategorySelection() {
        List<Category> categoryList = tryToGetCategory();
        if(categoryMenuButton.getItems() != null){
            categoryMenuButton.getItems().clear();
            categoryList.stream()
                    .map(category -> {
                        CheckMenuItem menuItem = new CheckMenuItem();
                        menuItem.setText(category.name().getValue());

                        return menuItem;
                    })
                    .forEach(menuItem -> categoryMenuButton.getItems().add(menuItem));
        }
    }

    /**
     * method that will construct new Movie from user input and tries to create it
     * this is two way purpose and the whole logic depends on the boolean value of editable
     * @param e action event
     */
    @FXML
    private void movieOnClickAction(ActionEvent e) {
        if(!isEditable){
            if(isValidatedInput()){
                var collectedCategory = mapSelectedCategories();
//                Movie movie = new Movie(
//                        0,
//                        new SimpleStringProperty(movieName.getText().trim()),
//                        personalRatingSpin.getValue(),
//                        new SimpleStringProperty(path.getText().trim()),
//                        collectedCategory,
//                        null);

                Movie2 movie2 = new Movie2();
                movie2.setName(movieName.getText().trim());
                movie2.setRating(personalRatingSpin.getValue());
                movie2.setAbsolutePath(path.getText().trim());
                movie2.setCategories(collectedCategory);
                int result = tryCreateMovie(movie2);
                closeAndUpdate(result,movie2.getId());
                e.consume();
                try {
                    movieModel.getAllMovies();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }else {
            if(isValidatedInput()){
                var collectedCategory = mapSelectedCategories();

                Movie2 movie2 = new Movie2();
                movie2.setId(editableMovie.id());
                movie2.setName(movieName.getText().trim());
                movie2.setRating(personalRatingSpin.getValue());
                movie2.setAbsolutePath(path.getText().trim());
                movie2.setCategories(collectedCategory);

                int result = tryUpdateMovie(movie2);
                closeAndUpdate(result,movie2.getId());

                e.consume();
//                var collectedCategory = mapSelectedCategories();
//                Movie movie = new Movie(
//                        editableMovie.id(),
//                        new SimpleStringProperty(movieName.getText().trim()),
//                        personalRatingSpin.getValue(),
//                        new SimpleStringProperty(path.getText().trim()),
//                        collectedCategory,
//                        editableMovie.lastview());
//
//                int result = tryUpdateMovie(movie);
//                closeAndUpdate(result,movie.id());
//                e.consume();
            }

        }
    }

    /**
     * method that sets movie to be editable from passed movie object
     * @param movie object that will be displayed to be edited
     */
    protected void setEditableView(Movie movie){
        isEditable = true;
        editableMovie = movie;

        movieName.setText(editableMovie.name().getValue());

        SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 10.0, editableMovie.rating(),0.5);
        personalRatingSpin.setValueFactory(valueFactory);
        path.setText(editableMovie.absolutePath().getValue());

        if(categoryMenuButton.getItems() != null){
            categoryMenuButton.getItems().clear();
            setEditableCategories();

        }
        confirm_action.setText("Update");
        labelMovieWindow.setText("Update Movie");
    }

    /**
     * load and sets correctly all the categories depends if movie has them
     */
    private void setEditableCategories() {
        List<Category> categoryList = tryToGetCategory();
        Set<String> categorySet = editableMovie.categories().stream().map(category -> category.name().getValue()).collect(Collectors.toSet());

        categoryList.stream()
                .map(category -> {
                    CheckMenuItem menuItem = new CheckMenuItem();
                    menuItem.setText(category.name().getValue());

                    if(categorySet.contains(category.name().getValue())){
                        menuItem.setSelected(true);
                    }

                    return menuItem;
                })
                .forEach(menuItem -> categoryMenuButton.getItems().add(menuItem));
    }

    /**
     * validates user input
     * @return false is requirements not met
     */
    private boolean isValidatedInput() {
        boolean isValidated = false;
        if(movieName.getText().isEmpty() || mapSelectedCategories().isEmpty() || path.getText().isEmpty()){
            AlertHelper.showDefaultAlert("Please fill all the field! You get the drill" , Alert.AlertType.ERROR);
        }else  {
            isValidated = true;
        }
        return isValidated;
    }

    /**
     * maps selected categories
     * @return list of Categories
     */
    private List<Category2> mapSelectedCategories() {
//        return categoryMenuButton.getItems().stream()
//                .filter(item -> item instanceof CheckMenuItem)
//                .map(CheckMenuItem.class::cast)
//                .filter(CheckMenuItem::isSelected)
//                .map(button ->  new Category2(0, button.getText()))
//                .toList();

        List<Category2> categories = new ArrayList<>();
        categoryMenuButton.getItems().stream()
                .filter(item -> item instanceof CheckMenuItem)
                .map(CheckMenuItem.class::cast)
                .filter(CheckMenuItem::isSelected)
                .forEach(button -> {
                    Category2 category = new Category2();
                    category.setName(button.getText());
                    categories.add(category);
                });
        return categories;
    }

    /**
     * method to check if result was > 0 and decide if refresh table and close or notifies user that something went wrong
     * @param result from model with stored int
     * @param id of the movie that is currently being deleted
     */
    private void closeAndUpdate(int result,int id) {
        if(result > 0){
            movieController.refreshTable();
            getStage().close();
            AlertHelper.showDefaultAlert("Success with id: "+ id, Alert.AlertType.INFORMATION);
        }else {
            AlertHelper.showDefaultAlert("Successfully error occurred with id: "+ id, Alert.AlertType.ERROR);
        }

    }

    /**
     * method that tries to pass movie object and create it in database
     * @param movie object that will be created
     * @return 0 or 1 where 0 is fail and 1 is success
     */
    private int tryCreateMovie(Movie2 movie) {
        return movieModel.createMovie(movie);
    }

    /**
     * method that tries to pass movie object and update it in database
     * @param movie object that will be updated
     * @return 0 or 1 where 0 is fail and 1 is success
     */

    private int tryUpdateMovie(Movie2 movie) {
            return movieModel.updateMovie(movie);
    }

    /**
     * tries to get the List of categories
     * @return list of Categories with id,name
     */
    private List<Category> tryToGetCategory() {
        List<Category2> categories = categoryModel.getAllCategories();
        List<Category> categories1 =
                categories.stream().map(c -> new Category(c.getId(),new SimpleStringProperty(c.getName()))).toList();
            return categories1;

    }
}

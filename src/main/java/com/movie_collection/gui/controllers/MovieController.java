package com.movie_collection.gui.controllers;

import com.google.inject.Inject;
import com.movie_collection.be.Category;
import com.movie_collection.be.Movie;
import com.movie_collection.be.Movie2;
import com.movie_collection.bll.helpers.ViewType;
import com.movie_collection.bll.utilities.AlertHelper;
import com.movie_collection.gui.DTO.MovieDTO;
import com.movie_collection.gui.controllers.abstractController.RootController;
import com.movie_collection.gui.controllers.controllerFactory.IControllerFactory;
import com.movie_collection.gui.models.IMovieModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.MoveTo;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for Movies with the view
 */
public class MovieController extends RootController implements Initializable {

    // -> Optional table description
    @FXML
    private ImageView movieImage;
    @FXML
    private TextArea desPlot;
    @FXML
    private Label desReleased,
            desRunTime,
            desCast,
            desDirector,
            descrIReleased1,
            desImdbRating, desPrRating,
            descrMovieTitle, descrIMDBRating;

    @FXML
    private TableView<Movie> moviesTable;
    @FXML
    private TableColumn<Movie, Button> colPlayMovie, colEditMovies, colDeleteMovie;
    @FXML
    private TableColumn<Movie, String> colMovieTitle, movieYear, colMovieCategory;
    @FXML
    private TableColumn<Movie, String> colMovieRating;

    private final IMovieModel movieModel;

    private static String txtContent = ""; // what is this ??

    private final IControllerFactory controllerFactory;

    private boolean isCategoryView = false;

    private int categoryId;

    @Inject
    public MovieController(IMovieModel movieService, IControllerFactory controllerFactory) {
        this.movieModel = movieService;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fillTableWithData();
        listenToClickRow();
    }


    /**
     * method that check which row is selected and sets description
     */
    private void listenToClickRow() {
        moviesTable.setOnMouseClicked(event -> {
            Movie selectedMovie = moviesTable.getSelectionModel().getSelectedItem();
            if (selectedMovie != null) {
                // tries to find the movie by name
                MovieDTO movieDTO = movieModel.findMovieByNameAPI(selectedMovie.name().getValue());
                fillDescriptionWithAPIData(movieDTO,selectedMovie);
            }
        });
    }

    /**
     * fill all the description data
     * @param movieDTO
     * @param selectedMovie
     */
    private void fillDescriptionWithAPIData(MovieDTO movieDTO, Movie selectedMovie) {
        if(movieDTO.Poster != null){
            movieImage.setImage(new Image(movieDTO.Poster));
        }

        descrMovieTitle.setText(selectedMovie.name().getValue());
        desPlot.setText(movieDTO.Plot);
        desRunTime.setText(movieDTO.Runtime);
        desCast.setText(movieDTO.imdbRating);
        descrIReleased1.setText(movieDTO.Released);
        desImdbRating.setText(movieDTO.imdbRating);
        desDirector.setText(movieDTO.Director);
        descrMovieTitle.setText(selectedMovie.name().getValue());
        desPrRating.setText(String.valueOf(selectedMovie.rating()));
    }

    /**
     * method to fill table with initial data by the model
     */
    private void fillTableWithData() {
        // sets value factory for play column
        colPlayMovie.setCellValueFactory(col -> {
            Button playButton = new Button("▶️");
            playButton.setOnAction(e -> {
                actionPlay(col);
            });
            return new SimpleObjectProperty<>(playButton);
        });
        // ->
        colMovieTitle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name().getValue())); // set movie title
        movieYear.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name().getValue())); // does not have anything now from model
        colMovieRating.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().rating())));

        // sets value factory for movie category column data are collected by name and joined by "," -> action,horror
        colMovieCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().categories().stream()
                .map(Category::name)
                .map(StringProperty::getValue)
                .collect(Collectors.joining(","))
        ));
        // sets value factory for edit column
        colEditMovies.setCellValueFactory(col -> {
            Button editButton = new Button("⚙️");
            Movie updateMovie = col.getValue();
            editButton.setOnAction(e -> {
                CreateMovieController controller = loadSetEditController(updateMovie);
                showUpdateWindow(controller.getView());
            });
            return new SimpleObjectProperty<>(editButton);
        });
        // sets value factory for delete  column
        colDeleteMovie.setCellValueFactory(col -> {
            Button deleteButton = new Button("❌");
            deleteButton.setOnAction(e -> {
                Movie movie = col.getValue(); // get movie object from the current row
                if (movie != null) {
                    var resultNotify = AlertHelper.showOptionalAlertWindow("Are you sure you want delete movie with id: " + movie.id(), Alert.AlertType.CONFIRMATION);
                    if (resultNotify.get().equals(ButtonType.OK)) {
                        int result = tryDeleteMovie(movie.id()); // tries to delete movie by id inside the row
                        refreshTableAndNotify(result,movie.id());
                    }
                }
            });
            return new SimpleObjectProperty<>(deleteButton);
        });

        trySetTableWithMovies();
    }

    private void playVideoDesktop(int id, String path) throws IOException, InterruptedException {
        Runtime runTime = Runtime.getRuntime();
        if (!txtContent.isEmpty()) {
            String s[] = new String[]{txtContent, path};
            try {
                movieModel.updateTimeStamp(id);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            runTime.exec(s);
        } else {
            try {
                showMediaPlayerUnselected();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }


        trySetTableWithMovies();

    }

    protected void setIsCategoryView(int categoryId) {
        this.isCategoryView = true;
        this.categoryId = categoryId;

        if (moviesTable != null) {
            moviesTable.getItems().clear();
            trySetTableByCategory(categoryId);
        }
    }


    private void showUpdateWindow(Parent view) {
        Stage stage = new Stage();
        Scene scene = new Scene(view);

        stage.initOwner(getStage());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Update Movie");

        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private CreateMovieController loadSetEditController(Movie updateMovie) {
        CreateMovieController controller;
        try {
            controller = (CreateMovieController) controllerFactory.loadFxmlFile(ViewType.CREATE_EDIT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        controller.setEditableView(updateMovie);
        return controller;
    }

    /**
     * method that tries to delete movie by id
     * result success if > 0 ... else err display/handel
     *
     * @param id of movie that will be deleted
     */
    private int tryDeleteMovie(int id) {
        try {
            return movieModel.deleteMovie(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * refreshed the table and notify user about the status of his actions
     *
     * @param result that will be judged up on
     * @param id     that will be displayed if action for that id was successful
     */

    private void refreshTableAndNotify(int result, int id) {
        if (result > 0) {
            refreshTable();
            AlertHelper.showDefaultAlert("Successfully deleted movie with id: " + id, Alert.AlertType.INFORMATION);
        } else {
            AlertHelper.showDefaultAlert("Could not delete movie with id: " + id, Alert.AlertType.ERROR);
        }
    }


    private void showMediaPlayerUnselected() throws SQLException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Select your Media Player");
        alert.getButtonTypes().setAll(new ButtonType("OK"));
        Optional<ButtonType> btn = alert.showAndWait();
    }


    /**
     * method that clears table items if they are not null and sets it back to required values
     */

    protected void refreshTable() {
        if(moviesTable != null){
            if(moviesTable.getItems() != null){
                moviesTable.getItems().clear();
//                try {
                ObservableList<Movie2> movie = movieModel.getAllMovies();
                var test = movie.stream()
                        .map(m -> {
                            List<Category> movieCategoriesList = m.getCategories().stream()
                                    .map(c -> new Category(c.getId(), new SimpleStringProperty(c.getName())))
                                    .collect(Collectors.toList());
                            return new Movie(m.getId(), new SimpleStringProperty(m.getName()), m.getRating(), new SimpleStringProperty(m.getAbsolutePath()), m.getLastview(), movieCategoriesList);
                        }).toList();

                moviesTable.getItems().setAll(test);
            }
        }
    }

    private void actionPlay(TableColumn.CellDataFeatures<Movie, Button> col) {
        try {
            playVideoDesktop(col.getValue().id(), col.getValue().absolutePath().getValue());
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }


    private void trySetTableByCategory(int categoryId){
        ObservableList<Optional<List<Movie2>>> movie = movieModel.getAllMoviesInTheCategory(categoryId);

        if(!movie.isEmpty()){
            List<Movie> test = movie.stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(List::stream)
                    .map(m -> {
                        List<Category> movieCategoriesList = m.getCategories().stream()
                                .map(c -> new Category(c.getId(), new SimpleStringProperty(c.getName())))
                                .collect(Collectors.toList());
                        return new Movie(m.getId(), new SimpleStringProperty(m.getName()), m.getRating(), new SimpleStringProperty(m.getAbsolutePath()), m.getLastview(), movieCategoriesList);
                    }).toList();
            moviesTable.getItems().setAll(test);

        }else {
            moviesTable.getItems().setAll(List.of());
        }
    }

    /**
     * method that tries to set table with all movies
     */

    private void trySetTableWithMovies() {
        ObservableList<Movie2> movie = movieModel.getAllMovies();
         var test = movie.stream()
                .map(m -> {
                    List<Category> movieCategoriesList = m.getCategories().stream()
                            .map(c -> new Category(c.getId(), new SimpleStringProperty(c.getName())))
                            .collect(Collectors.toList());
                    return new Movie(m.getId(), new SimpleStringProperty(m.getName()), m.getRating(), new SimpleStringProperty(m.getAbsolutePath()), m.getLastview(), movieCategoriesList);
                }).toList();

            moviesTable.getItems().setAll(test);
    }

    /**
     * method that tries to delete movie by id
     * result success if > 0 ... else err display/handel
     * @param id of movie that will be deleted
     */
    private int tryDeleteMovie(int id) {
        try {
            return movieModel.deleteMovie(id);
        } catch (SQLException e) {
        moviesTable.setItems(movieModel.getFilteredMovies());
    }

    protected void setPath(Path fileName, String mediaPlayerPath) {
        try {
            Files.writeString(fileName, mediaPlayerPath);
            txtContent = Files.readString(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    }


}

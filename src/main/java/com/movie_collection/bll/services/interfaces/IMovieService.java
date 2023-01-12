package com.movie_collection.bll.services.interfaces;

import com.movie_collection.be.Movie;
import com.movie_collection.bll.helpers.CompareSigns;

import java.sql.SQLException;
import java.util.List;

public interface IMovieService {
    /**
     * method tyo get list of all movies
     *
     * @return List<Movie> to be created
     */
    List<Movie> getAllMovies() throws SQLException;

    /**
     * method to create a movie
     *
     * @param movie to be created
     * @return number of rows affected
     * @throws SQLException if the connection to the database fails
     */
    int createMovie(Movie movie) throws SQLException;

    /**
     * method to update a movie
     * @param movie to be updated with new parameters
     * @return number of rows affected
     * @throws SQLException if the connection to the database fails
     */
    int updateMovie(Movie movie) throws SQLException;

    /**
     * method to delete a movie
     *
     * @param id of the movie to be deleted
     * @return number of rows affected
     * @throws SQLException if the connection to the database fails
     */
    int deleteMovie(int id) throws SQLException;

    /**
     * Update timeStamp when a movie is played based on the id
     *
     * @param id id of movie to be played
     * @return number of rows affected
     * @throws SQLException if the connection to the database fails
     */
    int updateTimeStamp(int id) throws SQLException;

    /**
     * method to get all movies in a category
     *
     * @param categoryId of the category
     * @return List of movies in the category
     * @throws SQLException if the connection to the database fails
     */
    List<Movie> getAllMoviesInTheCategory(int categoryId) throws SQLException;

    /**
     * docs
     *
     * @param listToSearch
     * @param query
     * @param buttonValue
     * @param spinnerValue
     * @return
     */

    List<Movie> searchMovie(List<Movie> listToSearch, String query, CompareSigns buttonValue, double spinnerValue);
}

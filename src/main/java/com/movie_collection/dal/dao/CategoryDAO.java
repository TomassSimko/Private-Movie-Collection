package com.movie_collection.dal.dao;


import com.movie_collection.be.Category;
import com.movie_collection.dal.interfaces.ICategoryDAO;
import com.movie_collection.dal.mappers.CategoryMapperDAO;
import com.movie_collection.dal.myBatis.MyBatisConnectionFactory;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDAO implements ICategoryDAO {

    Logger logger = LoggerFactory.getLogger(CategoryDAO.class);

    @Override
    public List<Category> getAllCategories() {
        List<Category> allCategories = new ArrayList<>();
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            CategoryMapperDAO mapper = session.getMapper(CategoryMapperDAO.class);
            allCategories = mapper.getAllCategories();
        } catch (Exception ex) {
            logger.atTrace().log("An error occurred mapping tables", ex);
        }
        return allCategories;
    }

    @Override
    public Optional<Category> getCategoryByName(String categoryName) {
        Category movie2 = new Category();
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            CategoryMapperDAO mapper = session.getMapper(CategoryMapperDAO.class);
            movie2 = mapper.getCategoryByName(categoryName);
        } catch (Exception ex) {
            logger.error("An error occurred mapping tables", ex);
        }
        return Optional.ofNullable(movie2);
    }

    @Override
    public int createCategory(Category category) {
        int finalAffectedRows = 0;
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            CategoryMapperDAO mapper = session.getMapper(CategoryMapperDAO.class);
            int affectedRows = mapper.createCategory(category);
            session.commit();
            return affectedRows > 0 ? category.getId() : 0; //  after commit if rows > 0 returns the generated key
        } catch (Exception ex) {
            logger.error("An error occurred mapping tables", ex);
        }
        return finalAffectedRows;
    }

    @Override
    public int deleteCategory(int id) {
        int finalAffectedRows = 0;
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            CategoryMapperDAO mapper = session.getMapper(CategoryMapperDAO.class);
            int result = mapper.deleteCategory(id);
            session.commit();
            return result;
        } catch (Exception ex) {
            logger.error("An error occurred mapping tables", ex);
        }
        return finalAffectedRows;
    }

}
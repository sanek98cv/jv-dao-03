package com.internet.shop.dao.jdbc;

import com.internet.shop.dao.DriverDao;
import com.internet.shop.exception.DataProcessingException;
import com.internet.shop.lib.Dao;
import com.internet.shop.model.Driver;
import com.internet.shop.util.ConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Dao
public class DriverDaoJdbcImpl implements DriverDao {
    @Override
    public Driver create(Driver driver) {
        String createQuery = "INSERT INTO drivers (name, license_number, login, password) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(createQuery,
                        Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, driver.getName());
            statement.setString(2, driver.getLicenceNumber());
            statement.setString(3, driver.getLogin());
            statement.setString(4, driver.getPassword());
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                driver.setId(resultSet.getObject(1, Long.class));
            }
            return driver;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't create " + driver + " in DB", e);
        }
    }

    @Override
    public Optional<Driver> get(Long id) {
        String getQuery = "SELECT * FROM drivers WHERE id =? AND deleted = FALSE";
        Driver driver = null;
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(getQuery)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                driver = createDriver(resultSet);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get driver id + " + id + " from DB", e);
        }
        return Optional.ofNullable(driver);
    }

    @Override
    public Driver update(Driver driver) {
        String updateQuery = "UPDATE drivers SET name = ?, license_number = ?, "
                + "login = ?, password = ?"
                + "WHERE id = ? AND deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setString(1, driver.getName());
            statement.setString(2, driver.getLicenceNumber());
            statement.setString(3, driver.getLogin());
            statement.setString(4, driver.getPassword());
            statement.setLong(5, driver.getId());
            statement.executeUpdate();
            return driver;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't update " + driver + " in DB", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String deleteQuery = "UPDATE drivers SET deleted = TRUE  WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't delete driver id "
                    + id + " from DB", e);
        }
    }

    @Override
    public List<Driver> getAll() {
        String getAllQuery = "SELECT * FROM drivers WHERE deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(getAllQuery)) {
            ResultSet resultSet = statement.executeQuery();
            List<Driver> allDrivers = new ArrayList<>();
            while (resultSet.next()) {
                allDrivers.add(createDriver(resultSet));
            }
            return allDrivers;
        } catch (SQLException e) {
            throw new RuntimeException("Can`t get all drivers from DB ", e);
        }
    }

    private Driver createDriver(ResultSet resultSet) {
        try {
            String name = resultSet.getString("name");
            String license = resultSet.getString("license_number");
            Long id = resultSet.getObject("id", Long.class);
            String login = resultSet.getString("login");
            String password = resultSet.getString("password");
            Driver driver = new Driver(name, license);
            driver.setLogin(login);
            driver.setPassword(password);
            driver.setId(id);
            return driver;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't parse driver from ResultSet", e);
        }
    }

    @Override
    public Optional<Driver> findByLogin(String login) {
        String findQuery = "SELECT * FROM drivers WHERE login = ? AND deleted = FALSE;";
        Driver driver = null;
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(findQuery)) {
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                driver = createDriver(resultSet);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't find driver login" + login, e);
        }
        return Optional.ofNullable(driver);
    }
}

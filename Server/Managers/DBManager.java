package lab5.Server.Managers;



import lab5.Common.Models.*;
import lab5.Common.Tools.ExecutionResponse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class DBManager {

    private Connection connection;

    /// конструктор

    public DBManager(String filename, String url) {
        Properties info = new Properties();//работа с файлами конфигуряций
        try {
            info.load(new FileInputStream(filename));//загружаю сюда свой конфиг (my.cfg)
            System.out.println("Файл загружен");
        }
        catch (FileNotFoundException e){ }//файл не найден
        catch (IOException e){ }//ошибки чтения файла (нет прав доступа)

        //"jdbc:postgresql://db:5432/studs"  ----- подключение к этой базе данных (url)
        try {
            Class.forName("org.postgresql.Driver"); //попытка загрузить драйвер
        }catch (ClassNotFoundException e){
            System.out.println("Не получается загрузить драйвер, он не найден");
            e.printStackTrace();
        }
        try{
            connection = DriverManager.getConnection(url, info.getProperty("name"), info.getProperty("password"));
            System.out.println("Законнекчено с базой данных");
            connection.setAutoCommit(false);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Ошибка при законнекчивании с базой данных"+ e);
        }
    }

    /// завершение подключения

    public ExecutionResponse closeConnection(){
        try {
            connection.close();
            return new ExecutionResponse(true, "Подключение закрыто");
        }catch (Exception e){
            return new ExecutionResponse(false, "Ошибка при завершении подключения к бд"+e.getMessage());
        }
    }

    /// проверка пользователя

    public Integer checkUser(User user){
        String req = """
select * from "users" where login = ?""";// достаем пользователя
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, user.getName().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedSalt = rs.getString("salt");// достаем его соль и хэш
                    byte[] storedHash = rs.getBytes("hash");
                    if (checkPassword(user.getPassword(), storedHash, storedSalt)) {// проверяем пароль
                        return rs.getInt("user_id"); // Успех, возвращаем ид
                    }
                    return -1; // Неверный пароль
                }return -2; // Пользователь не найден
            }
        }catch (SQLException e){
            return -3;// Ошибка при проверке пользователя в бд
        }
        catch (Exception e){System.out.println(e.getMessage()); return -4;}
    }

    /// регистрация пользователя

    public ExecutionResponse regUser(User user){
        // Проверяем, свободно ли имя
        int res = checkUser(user);
        if (res != -2) {
            return new ExecutionResponse(false, "Имя пользователя занято");
        }

        String req = """ 
                insert into "users" (login, hash, salt) values (?, ?, ?)""";
        try{
            //connection.setAutoCommit(false);
            // автоматически сгенерированный ид пользователя
            try (PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
                String salt = generateSalt(); // делаем соль
                byte[] hash = hashPassword(user.getPassword(), salt); // Хэш
                ps.setString(1, user.getName().toLowerCase());
                ps.setBytes(2, hash);
                ps.setString(3, salt);
                // проверка что 1 строка извинилась
                if (ps.executeUpdate() > 0) {
                    ResultSet keys = ps.getGeneratedKeys();// возвращает сет с сгенерированными ключами ( с одним )
                    if(keys.next()){
                        connection.commit();// сохраняем
                        return new ExecutionResponse(true, String.valueOf(keys.getInt(1)));// возвращаем id добавленного
                    }
                }
                connection.rollback();// откатываем назад
                return new ExecutionResponse(false, "Ошибка регистрации");
            }
        }catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Ошибка отката транзакции: " + ex.getMessage());
            }
            return new ExecutionResponse(false, "Ошибка при сохранении в базы данных: " + e.getMessage());
        }
    }

    /// чтение коллекции

    public ExecutionResponse readCollection(HashMap<Integer,MusicBand> collection, Map<Integer, Integer> usersElements){
        String sql = """
        SELECT *
        FROM "musicBand" m
        LEFT JOIN "coordinates" c ON m.coordinates_id = c.coordinates_id
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Coordinates coordinates = null;
                if (rs.getDouble("x")!=0) {
                    coordinates = new Coordinates(
                            rs.getDouble("x"),
                            rs.getLong("y")
                    );
                }
                Studio studio= new Studio(rs.getString("studio"));
                LocalDate localDate = LocalDate.parse(rs.getDate("creationDate").toString());
                java.sql.Date zonedDateTime = java.sql.Date.valueOf(localDate);
                MusicBand musicband = new MusicBand(
                        rs.getInt("id"),
                        rs.getString("name"),
                        coordinates,
                        zonedDateTime,
                        rs.getInt("numberOfParticipants"),
                        rs.getLong("singlescount"),
                        rs.getInt("albumscount"),
                        MusicGenre.valueOf(rs.getString("genre")),
                        studio

                );

                collection.put(musicband.getId(), musicband);
                usersElements.put(musicband.getId(), rs.getInt("user_id"));

            }
            return new ExecutionResponse(true, "Коллекция загружена в коллекция");

        } catch (SQLException e) {
            System.err.println("Ошибка загрузки коллекции: " + e.getMessage());
            return new ExecutionResponse(false, "Ошибка чтения");
        }
    }

    /// добавление элемента в коллекцию
    public ExecutionResponse addElement(MusicBand musicBand, int userId) {
        // SQL-запросы для вставки в связанные таблицы
        String reqCoordinates = """
INSERT INTO "coordinates" (x, y) VALUES (?, ?)""";
        String reqMusicBand = """
                INSERT INTO "musicBand" (name, coordinates_id, "creationDate", "numberOfParticipants", "singlesCount", "albumsCount", genre, studio, user_id) VALUES (?, ?, ?, ?, ?, ?, ?::musicgenre, ?, ?)""";

        // ID для связанных записей
        Long coordinatesId = -1L;
        Long governorId = -1L;

        try {
            try (PreparedStatement psCoordinates = connection.prepareStatement(reqCoordinates, Statement.RETURN_GENERATED_KEYS)) {
                psCoordinates.setDouble(1, musicBand.getCoordinates().getX());
                psCoordinates.setDouble(2, musicBand.getCoordinates().getY());

                int affectedRows = psCoordinates.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = psCoordinates.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            coordinatesId = generatedKeys.getLong(1);
                        } else {
                            throw new SQLException("Не удалось получить ID координат");
                        }
                    }
                } else {
                    throw new SQLException("Ошибка при вставке координат");
                }
            }
            try (PreparedStatement psMusicBand = connection.prepareStatement(reqMusicBand, Statement.RETURN_GENERATED_KEYS)) {
                // Установка параметров
                psMusicBand.setString(1,musicBand.getName());
                psMusicBand.setLong(2, coordinatesId);
                psMusicBand.setDate(3, java.sql.Date.valueOf(musicBand.getCreationDate().toLocalDate()));
                psMusicBand.setInt(4, musicBand.getnumberOfParticipants());
                psMusicBand.setLong(5, musicBand.getSinglesCount());
                psMusicBand.setLong(6, musicBand.getAlbumsCount());
                psMusicBand.setString(7, musicBand.getGenre().name());
                psMusicBand.setString(8, musicBand.getStudio().toString());
                psMusicBand.setInt(9, userId);
                int affectedRows = psMusicBand.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = psMusicBand.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            connection.commit(); // Фиксируем всю транзакцию
                            return new ExecutionResponse(true, String.valueOf(generatedKeys.getLong(1)));
                        }
                    }
                }
                throw new SQLException("Ошибка при вставке музыкальной группы");
            }
            } catch (SQLException e) {
            try {
                connection.rollback(); // Откатываем при ошибке
            } catch (SQLException rollbackEx) {
                System.err.println("Ошибка при откате транзакции: " + rollbackEx.getMessage());
            }
            return new ExecutionResponse(false, "Ошибка при добавлении в базу данных: " + e.getMessage());
        }
    }


    public ExecutionResponse updateElement(MusicBand musicBand) {
        // SQL-запросы для обновления связанных данных
        String sqlUpdateCoordinates = """
                UPDATE "coordinates" SET x=?, y=? WHERE coordinates_id = 
                (SELECT coordinates_id FROM "musicBand" WHERE coordinates_id=?)""";
        String sqlUpdateMusicBand = """
                UPDATE "musicBand" SET name=?, "creationDate"=?, "numberOfParticipants"=?, "singlesCount"=?, 
                genre?::musicgenre, studio=?,  WHERE id=?""";

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement psCoordinates = connection.prepareStatement(sqlUpdateCoordinates)) {
                psCoordinates.setDouble(1, musicBand.getCoordinates().getX());
                psCoordinates.setDouble(2, musicBand.getCoordinates().getY());
                psCoordinates.setLong(3, musicBand.getId());

                int affectedRows = psCoordinates.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Координаты не найдены для обновления");
                }
            }
            try (PreparedStatement psMusicBand = connection.prepareStatement(sqlUpdateMusicBand)) {
                psMusicBand.setString(1, musicBand.getName());
                psMusicBand.setDate(2, java.sql.Date.valueOf(musicBand.getCreationDate().toLocalDate()));
                psMusicBand.setInt(3, musicBand.getnumberOfParticipants());
                psMusicBand.setLong(4, musicBand.getSinglesCount());
                psMusicBand.setInt(5, musicBand.getAlbumsCount());
                psMusicBand.setString(6, musicBand.getGenre().name());
                psMusicBand.setString(7, musicBand.getStudio().toString());
                psMusicBand.setLong(8, musicBand.getId());
                int affectedRows = psMusicBand.executeUpdate();
                if (affectedRows > 0) {
                    connection.commit(); // Фиксируем транзакцию
                    return new ExecutionResponse(true, "Музыкальная группа успешно обновлена");
                }
                throw new SQLException("музыкальная группа не найдена для обновления");
            }
        } catch (SQLException e) {
            try {
                connection.rollback(); // Откатываем при ошибке
            } catch (SQLException rollbackEx) {
                System.err.println("Ошибка при откате: " + rollbackEx.getMessage());
            }
            return new ExecutionResponse(true, "Ошибка обновления: " + e.getMessage());
        }

    }


    public ExecutionResponse removeElement(Long id) {

        String deleteMusicBandSql = """
        DELETE FROM "musicBand" WHERE id = ?""";

        try {
            try (PreparedStatement deleteCityStmt = connection.prepareStatement(deleteMusicBandSql)) {
                deleteCityStmt.setLong(1, id);
                int citiesDeleted = deleteCityStmt.executeUpdate();

                if (citiesDeleted >= 0) {
                    connection.commit(); // Фиксируем транзакцию
                    return new ExecutionResponse(true, "Успешно удалено музыкальных групп: " + citiesDeleted);
                }
                throw new SQLException("Не удалось удалить музыкальные группы");
            }
        } catch (SQLException e) {
            try {
                connection.rollback(); // Откатываем при ошибке
            } catch (SQLException rollbackEx) {
                System.err.println("Ошибка при откате транзакции: " + rollbackEx.getMessage());
            }
            return new ExecutionResponse(false, "Ошибка удаления коллекции: " + e.getMessage());
        }
    }
    public ExecutionResponse removeCollection(Long userId) {
        // SQL-запросы для удаления связанных данных
        String deleteCoordinatesSql = """
                DELETE FROM "coordinates" WHERE coordinates_id IN
                (SELECT coordinates_id FROM "musicBand" WHERE user_id = ?)""";
        String deleteMusicBandSql = """
        DELETE FROM "musicBand" WHERE user_id = ?""";

        try {
            // Отключаем авто-коммит для управления транзакцией
            //connection.setAutoCommit(false);

            int totalDeleted = 0;
            try (PreparedStatement deleteCoordinatesStmt = connection.prepareStatement(deleteCoordinatesSql)) {
                deleteCoordinatesStmt.setLong(1, userId);
                totalDeleted = deleteCoordinatesStmt.executeUpdate();
                // Не бросаем исключение если координатов нет (может быть NULL)
            }

            try (PreparedStatement deleteCityStmt = connection.prepareStatement(deleteMusicBandSql)) {
                deleteCityStmt.setLong(1, userId);
                int citiesDeleted = deleteCityStmt.executeUpdate();

                if (citiesDeleted >= 0) {
                    connection.commit(); // Фиксируем транзакцию
                    return new ExecutionResponse(true, "Успешно удалено музыкальных групп: " + citiesDeleted);
                }
                throw new SQLException("Не удалось удалить музыкальные группы");
            }
        } catch (SQLException e) {
            try {
                connection.rollback(); // Откатываем при ошибке
            } catch (SQLException rollbackEx) {
                System.err.println("Ошибка при откате транзакции: " + rollbackEx.getMessage());
            }
            return new ExecutionResponse(false, "Ошибка удаления коллекции: " + e.getMessage());
        }
    }

    public boolean checkPassword(String inputPassword, byte[] passwordHash, String salt) {
        if (inputPassword == null) return false;
        byte[] inputHash = hashPassword(inputPassword, salt);
        return Arrays.equals(passwordHash, inputHash);
    }
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16 байт = 128 бит
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private byte[] hashPassword(String password, String salt) {
        try {
            String saltedPassword = password + salt;
            MessageDigest md = MessageDigest.getInstance("MD2");
            return md.digest(saltedPassword.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка хэширования", e);
        }
    }
}
package com.dormitory.app.database;

import org.hsqldb.jdbc.JDBCDriver;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ServiceDatabase {
    static ArrayList<String> tables = new ArrayList<>(Arrays.asList("User", "Permission", "CommonNews",
            "MarketNews", "PictureCommon", "PictureMarket", "PinnedMessage", "Tag", "Color", "Liked"));
    public static void create(String tableName){
        try{
            Connection connection = SetConnection.getConnection();
            Statement statement = connection.createStatement();

            switch (tableName){
                case "User":
                    statement.execute("CREATE TABLE IF NOT EXISTS User (user_id int NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                            "login varchar(20) unique," +
                            "passw varchar(20)," +
                            "group_id int," +
                            "group_date timestamp," +
                            "group_duration int," +
                            "user_name varchar(20)," +
                            "user_surname varchar(20)," +
                            "isu_number int," +
                            "block_id varchar(20))");
                    break;
                case "Permission":
                    statement.execute("CREATE TABLE IF NOT EXISTS Permission (group_id int NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                            "see_new_com int," +
                            "see_new_mar int," +
                            "add_new_com int," +
                            "add_new_mar int," +
                            "like_dislike int," +
                            "see_contact int)");
                    statement.execute("INSERT INTO Permission(see_new_com, see_new_mar, add_new_com, add_new_mar, like_dislike, see_contact) VALUES (1, 1, 1, 1, 1, 1)"); // 0 - Админ
                    statement.execute("INSERT INTO Permission(see_new_com, see_new_mar, add_new_com, add_new_mar, like_dislike, see_contact) VALUES (1, 1, 0, 1, 1, 1)"); // 1 - Мега-студент
                    statement.execute("INSERT INTO Permission(see_new_com, see_new_mar, add_new_com, add_new_mar, like_dislike, see_contact) VALUES (1, 1, 0, 2, 2, 2)"); // 2 - Студент
                    break;
                case "CommonNews":
                    statement.execute("CREATE TABLE IF NOT EXISTS CommonNews (new_com_id int NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                            "title varchar(500)," +
                            "text varchar(3000)," +
                            "author_id int," +
                            "date timestamp," +
                            "tag_id int)");
                    break;
                case "MarketNews":
                    statement.execute("CREATE TABLE IF NOT EXISTS MarketNews (new_mar_id int NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                            "title varchar(500)," +
                            "text varchar(3000)," +
                            "author_id int," +
                            "date timestamp," +
                            "tag_id int," +
                            "contact_info varchar(500)," +
                            "rating int)");
                    break;
                case "PinnedMessage":
                    statement.execute("CREATE TABLE IF NOT EXISTS CommonNews (mes_id int NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                            "title varchar(500)," +
                            "text varchar(2000)," +
                            "color_id int)");
                    break;
                case "PictureCommon":
                    statement.execute("CREATE TABLE IF NOT EXISTS Tag (tag_id int NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                            "tag_text varchar(25)" +
                            ")");
                    break;
                case "Color":
                    statement.execute("CREATE TABLE IF NOT EXISTS Color (color_id int NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                            "red int," +
                            "green int," +
                            "blue int)");
                    break;
                case "Liked":
                    statement.execute("CREATE TABLE IF NOT EXISTS Liked (user_id int," +
                            "new_mar_id int)");
            }
            statement.close();
            connection.close();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }

    public static void createAll(){
        for (String tableName: tables){
            ServiceDatabase.create(tableName);
        }
    }

    public static void showAll(String tableName){
        try{
            Connection connection = SetConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName);

            ResultSet resultSet = statement.executeQuery();

            switch (tableName){
                case "User":
                    while (resultSet.next()){
                        int user_id = resultSet.getInt(1);
                        String login = resultSet.getString(2);
                        String passw = resultSet.getString(3);
                        int group_id = resultSet.getInt(4);
                        java.sql.Date date = resultSet.getDate(5);
                        int group_duration = resultSet.getInt(6);
                        String user_name = resultSet.getString(7);
                        String user_surname = resultSet.getString(8);
                        int isu_number = resultSet.getInt(9);
                        String block_id = resultSet.getString(10);
                        String common = user_id + " - " + login + " - " + passw + " - " + group_id + " - " + date + " - " + group_duration + " - " + user_name + " - " + user_surname + " - " + isu_number + " - " + block_id;
                        System.out.println(common);
                    }
                    break;
                case "Permission":
                    while (resultSet.next()){
                        int group_id = resultSet.getInt(1);
                        int see_new_com = resultSet.getInt(2);
                        int see_new_mar = resultSet.getInt(3);
                        int add_new_com = resultSet.getInt(4);
                        int add_new_mar = resultSet.getInt(5);
                        int like_dislike = resultSet.getInt(6);
                        int see_contact = resultSet.getInt(7);
                        System.out.println("Group: " + group_id + "; Able to:\n" + "see common news: " + see_new_com + ";\n" + "see market news: " + see_new_mar + ";\n" + "add common news: " + add_new_com + ";\n" + "add market news: " + add_new_mar + ";\n" + "like or dislike news: " + like_dislike + ";\n" + "see contacts: " + see_contact);
                    }
                    break;
            }


            statement.close();
            connection.close();

        }
        catch (Exception e){
        }
    }

    public static void addToUser(String login, String passw, int group_id, Date group_date, int group_duration, String user_name, String user_surname, int isu_number, String block_id){
        try {
            Connection connection = SetConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO User(login, passw, group_id, group_date, group_duration, user_name, user_surname, isu_number, block_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setString(1, login);
            statement.setString(2, passw);
            statement.setInt(3, group_id);
            statement.setDate(4, java.sql.Date.valueOf(String.valueOf(group_date)));
            statement.setInt(5, group_duration);
            statement.setString(6, user_name);
            statement.setString(7, user_surname);
            statement.setInt(8, isu_number);
            statement.setString(9, block_id);
            statement.execute();
            statement.close();
            connection.close();

        }
        catch (Exception e){}

    }

    private static void dropAll(){
        for (String tableName: tables){
            ServiceDatabase.drop(tableName);
        }
    }

    private static void drop(String table){
        try {
            Connection connection = SetConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement("DROP TABLE IF EXISTS " + table);
            statement.execute();
            statement.close();
            connection.close();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }

    public static void main(String[] args) {
        // ServiceDatabase.dropAll();
        // ServiceDatabase.createAll();
        // ServiceDatabase.addToUser("Fant1k43", "123456", 0, java.sql.Date.valueOf("2020-12-13"), 999999999, "Никита", "Дукин", 284744, "803б");
        // ServiceDatabase.addToUser("_jovan", "1488", 1, java.sql.Date.valueOf("2020-12-13"), 999999999, "Иван", "Шахтаров", 285649, "1205б");
        // ServiceDatabase.addToUser("nick", "654321", 2, java.sql.Date.valueOf("2020-12-13"), 999999999, "Никита", "Мангутов", 286543, "803б");
        // ServiceDatabase.showAll("Permission");
        // ServiceDatabase.drop("Tags");
    }
}

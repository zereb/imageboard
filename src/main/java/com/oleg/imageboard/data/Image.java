package com.oleg.imageboard.data;

import com.oleg.imageboard.Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Image {
    public final long id;
    public final String name;

    public Image(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString(){
        return "id: " + id + " " + name;
    }

    public static Image insertInDB(Image record){
        try (Connection connection = Server.db.getConnection();
             PreparedStatement prep = connection.prepareStatement("insert into images (id, name) values (?,?)")
        ){
            prep.setLong(1, record.id);
            prep.setString(2, record.name);
            prep.execute();
            System.out.println("Added record: " + record.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return record;
    }
}

package com.oleg.imageboard.data;

import com.oleg.imageboard.H2SQLConnect;
import com.oleg.imageboard.Server;
import free.zereb.utils.Utils;

import java.sql.*;
import java.util.*;

public class Post {
    public final int id;
    public final long tId;
    public final long epoch;
    public final String email;
    public final ArrayList<String> images;
    public final String text;
    public final ArrayList<Integer> responses;
    private String humanEpoch;

    public Post(int id, long tId, long epoch, String email, ArrayList<String> images, String text, ArrayList<Integer> responses) {
        this.id = id;
        this.tId = tId;
        this.epoch = epoch;
        this.responses = responses;
        if (email.isBlank())
            email = "Anon";
        this.email = email;
        this.images = images;
        this.text = text;
        humanEpoch = Utils.getHumanEpoch(epoch / 1000);
    }


    @Override
    public String toString() {
        String he = Utils.getHumanEpoch(epoch / 1000);
        StringBuilder imgs = new StringBuilder();
        images.forEach(s -> imgs.append(s).append("\n"));
        return String.format("id: %d tId: %d time: %s email: %s \n %s \n %s"
                , id, tId, epoch, email, images, text);
    }


    public static void insertInDB(Post record){
        try (Connection connection = Server.db.getConnection();
             PreparedStatement prep = connection.prepareStatement("insert into posts (id, tid, epoch, email, images, text, responses) values (?,?,?,?,?,?,?)")
        ){
            prep.setInt(1, record.id);
            prep.setLong(2, record.tId);
            prep.setLong(3, record.epoch);
            prep.setString(4, record.email);
            prep.setArray(5, connection.createArrayOf("text", record.images.toArray()));
            prep.setString(6, record.text);
            prep.setArray(7, connection.createArrayOf("int", record.responses.toArray()));
            prep.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void  updateResps(int postId, ArrayList<Integer> resps){
       try(Connection connection = Server.db.getConnection();
           PreparedStatement prep = connection.prepareStatement("update posts set responses = ? where id = ?")
       ){
           prep.setArray(1,connection.createArrayOf("int", resps.toArray()));
           prep.setInt(2, postId);
           prep.execute();
       }catch (SQLException e){
           e.printStackTrace();
       }
    }

    public static ArrayList<Post> getPosts(H2SQLConnect connect, String sql) {
        ArrayList<Post> posts = new ArrayList<>();
        try(Connection connection = connect.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
        ){
            while (resultSet.next()) {
                Array imgs = resultSet.getArray(5);
                Array resps = resultSet.getArray(7);
                Object[] images = (Object[]) imgs.getArray();
                ArrayList<String> arrayList = new ArrayList<>();
                for (Object image : images)
                    arrayList.add((String) image);
                Object[] responses = (Object[]) resps.getArray();
                ArrayList<Integer> responsesArrList = new ArrayList<>();
                for (Object response: responses)
                    responsesArrList.add((Integer) response);
                posts.add(new Post(
                            resultSet.getInt(1),
                            resultSet.getLong(2),
                            resultSet.getLong(3),
                            resultSet.getString(4),
                            arrayList,
                            resultSet.getString(6),
                            responsesArrList
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }
}


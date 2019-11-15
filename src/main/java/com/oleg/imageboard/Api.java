package com.oleg.imageboard;

import com.oleg.imageboard.data.Image;
import com.oleg.imageboard.data.Post;
import com.oleg.imageboard.data.Response;
import io.javalin.core.util.FileUtil;
import io.javalin.http.Handler;
import io.javalin.plugin.json.JavalinJson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Api {
    public static Handler getPosts = ctx -> {
        String sql = "SELECT * FROM posts WHERE tid = " + ctx.pathParam("id");
        ArrayList<Post> posts = Post.getPosts(Server.db, sql);
        ctx.json(new Response("", posts));
    };



    public static  Handler getThreads = ctx -> {
        String sql = "";
        if (!ctx.pathParamMap().containsKey("id"))
            sql = "SELECT tid FROM posts";
        else
            sql = "select id from posts where tid = " +ctx.pathParam("id");

        HashSet<Long> threads = new HashSet<>();

        try(Connection connection = Server.db.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
        ){
            while (resultSet.next()){
                threads.add(resultSet.getLong(1));
            }
        }catch (Exception e){
            ctx.json(new Response(e.toString(), ""));
        }

        ctx.json(new Response("", threads));
    };


    public static Handler createThread = ctx -> {
        String json = ctx.queryParam("data", String.class).get();
        Long tid = System.currentTimeMillis();
        Post post = JavalinJson.fromJson(json, Post.class);
        System.out.println(post.toString());
        if (post.images.isEmpty()){
            ctx.json(new Response("Error. 0 pictures.", ""));
            return;
        }
        if (post.text.length() < 5){
            ctx.json(new Response("Error! Message is too litle.", ""));
            return;
        }
        Post.insertInDB(new Post(
                generateId(),
                tid,
                System.currentTimeMillis(),
                post.email,
                post.images,
                post.text
        ));

        ctx.json(new Response("", tid));

    };

    public static Handler createPost = ctx -> {
        String json = ctx.queryParam("data", String.class).get();
        Post post = JavalinJson.fromJson(json, Post.class);
        System.out.println(post.text.length());
        if (post.text.length() < 10){
            ctx.json(new Response("Error! Message is too litle.", ""));
            return;
        }
        Post.insertInDB(new Post(
                generateId(),
                Long.parseLong(ctx.pathParam("id")),
                System.currentTimeMillis(),
                post.email,
                post.images,
                post.text
        ));

        ctx.json(new Response("", "Created new post"));

    };
    public static Handler getImages = ctx -> {
        String sql;
        if (ctx.pathParamMap().containsKey("id"))
            sql= "select * from images where id = " + ctx.pathParam("id");
        else
            sql = "select * from images";

        try (Connection connection = Server.db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery();
        ){
            ArrayList<Image> images = new ArrayList<>();
            while (resultSet.next()){
                images.add(new Image(
                        resultSet.getLong(1),
                        resultSet.getString(2))
                );
            }
            ctx.json(new Response("", images));
        }catch (Exception e){
            e.printStackTrace();
        }
    };
    public static Handler uplodaImg = ctx -> {
        ArrayList<Image> images = new ArrayList<>();
        ctx.uploadedFiles("files").forEach(file -> {
            System.out.println(file.getFilename());
            String filename = "[" + System.currentTimeMillis() + "]" + file.getFilename();
            FileUtil.streamToFile(file.getContent(), "upload/" + filename);
            images.add(Image.insertInDB(new Image(System.nanoTime(), filename)));
        });
        ctx.json(new Response("", images));
    };

    public static  int generateId(){
        try (Connection connection = Server.db.getConnection();
             PreparedStatement statement = connection.prepareStatement("select id from posts order by id desc limit 1 ");
             ResultSet resultSet = statement.executeQuery();
        ){
            while (resultSet.next())
                return resultSet.getInt(1) + 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
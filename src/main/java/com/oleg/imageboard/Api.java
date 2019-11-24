package com.oleg.imageboard;

import com.oleg.imageboard.data.Image;
import com.oleg.imageboard.data.Post;
import com.oleg.imageboard.data.Response;
import io.javalin.core.util.FileUtil;
import io.javalin.http.Handler;
import io.javalin.plugin.json.JavalinJson;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Api {
    public static Handler getPosts = ctx -> {
        String sql = "SELECT * FROM posts WHERE tid = " + ctx.pathParam("id");
        ArrayList<Post> posts = Post.getPosts(Server.db, sql);
        ctx.json(new Response("", posts));
    };


    public static Handler getPost = ctx -> {
        String sql = "SELECT * FROM posts WHERE id = " + ctx.pathParam("id");
        ArrayList<Post> posts = Post.getPosts(Server.db, sql);
        ctx.json(new Response("", posts));
    };


    public static  Handler getThreads = ctx -> {
        String sql = "SELECT tid, epoch FROM posts order by epoch desc";


        LinkedHashSet<Long> threads = new LinkedHashSet<>();

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
        String json = ctx.formParam("data", String.class).get().replaceAll("&&", "");
        System.out.println(json);
        Long tid = System.currentTimeMillis();
        Post post = JavalinJson.fromJson(json, Post.class);
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
                post.text,
                new ArrayList<>()));

        ctx.json(new Response("", tid));

    };

    public static Handler createPost = ctx -> {
        String json = ctx.formParam("data", String.class).get();
        Post post = JavalinJson.fromJson(json, Post.class);
        Long tid = Long.parseLong(ctx.pathParam("id"));
        if (post.text.length() < 4){
            ctx.json(new Response("Error! Message is too litle.", ""));
            return;
        }
        int generatedId = generateId();

        Pattern pattern = Pattern.compile(">>[0-9]+");
        Matcher matcher = pattern.matcher(post.text);
        while (matcher.find()) {
            int id = Integer.parseInt(matcher.group().replaceAll(">>", ""));
            String sql = "SELECT * FROM posts WHERE id = " + id;
            ArrayList<Post> posts = Post.getPosts(Server.db, sql);
            posts.forEach(post1 -> {
                post1.responses.add(generatedId);
                Post.updateResps(id, post1.responses);
            });
        }


        Post.insertInDB(new Post(
                generatedId,
                Long.parseLong(ctx.pathParam("id")),
                System.currentTimeMillis(),
                post.email,
                post.images,
                post.text,
                new ArrayList<>()));

        ctx.json(new Response("", tid));

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
            System.out.println("Uploaded file: " + file.getFilename());
            String namePrefix = String.valueOf(System.currentTimeMillis());
            String filename = namePrefix + "_" + file.getFilename();
            FileUtil.streamToFile(file.getContent(), "upload/" + filename);
            try {
                BufferedImage bi = ImageIO.read(Files.newInputStream(Path.of("upload/"+filename)));
                BufferedImage thumb = Utils.createThumb(bi, 200, 200);
                String extention = filename.substring(filename.lastIndexOf(".")+1);
                ImageIO.write(thumb, extention, Files.newOutputStream(Paths.get("upload/thumb_"+filename)));
                System.out.println("thumb created: thumb_" + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

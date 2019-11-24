package com.oleg.imageboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oleg.imageboard.data.Post;
import free.zereb.utils.arguments.ArgumentHandler;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.core.compression.Brotli;
import io.javalin.core.compression.Gzip;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.json.JavalinJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    public static String PASSWORD;
    public static String LOGIN;
    public static String DB_NAME;
    public static String PORT;

    public static H2SQLConnect db;

    private Server(){
        System.out.println(PASSWORD + " " + LOGIN + " " + DB_NAME);
        db = new H2SQLConnect();
        Gson gson = new GsonBuilder().create();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);


        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json; charset=utf-8";
            config.enforceSsl =false;
            config.enableCorsForAllOrigins();
            config.requestCacheSize = 256000L;
            new File("./upload").mkdirs();
            config.addStaticFiles("./upload/", Location.EXTERNAL);
            config.addStaticFiles("dist/",Location.CLASSPATH);
            config.requestLogger(((ctx, executionTimeMs) -> {
                String output = String.format("%s ip: %s %s took %f ms",
                        ctx.method(), ctx.ip(), ctx.path(), executionTimeMs);
                LOG.info(output);
            }));
        }).start(Integer.parseInt(PORT));


        ApiBuilder.setStaticJavalin(app);
        app.routes(() -> path("api", () -> {
            path("threads", () ->{
                get(Api.getThreads);
                post(Api.createThread);
                path(":id", () -> {
                    get(Api.getPosts);
                    post(Api.createPost);
                });
            });
            path("posts", () -> {path(":id", () -> {
                get(Api.getPost);
            });});
            path("images", () ->{
                get(Api.getImages);
                post(Api.uplodaImg);
                path(":id", () -> {
                    get(Api.getImages);
                });
            });
        }));



    }

    public static void main(String[] args) {
        new ArgumentHandler()
                .setArgument("-p", a -> PASSWORD = findArg("-p", a))
                .setArgument("-l", a -> LOGIN = findArg("-l", a))
                .setArgument("-db", a -> DB_NAME = findArg("-db", a))
                .setArgument("-port", a -> PORT = findArg("-port", a))
                .runArguments(args);
        new Server();

    }


    private static String findArg(String s, String[] args){
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith(s))
                return args[i+1];
        }
        throw new NullPointerException("Failed to find requried arg " + s);
    }
}

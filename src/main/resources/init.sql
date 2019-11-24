create table if not exists posts(
        id int primary key,
        tid bigint,
        epoch bigint,
        email varchar(255),
        images array,
        text text,
        responses array
);

create table if not exists images(
        id bigint,
        name varchar(255)
);
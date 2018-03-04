package me.philcali.service.reflection;

import java.awt.print.Book;
import java.util.Arrays;
import java.util.List;

import me.philcali.service.annotations.GET;
import me.philcali.service.annotations.request.PathParam;

public class BookResource {

    @GET("/books/{id}")
    public Book get(@PathParam final String id) {
        return book;
    }

    @GET("/books")
    public List<Book> list() {
        return Arrays.asList(book);
    }

}

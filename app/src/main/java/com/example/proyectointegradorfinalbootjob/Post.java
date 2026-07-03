package com.example.proyectointegradorfinalbootjob;
public class Post {
    public int id;
    public String author, initials, career, company, role, date, title, content;
    public int likes, comments;
    public boolean liked;

    public Post(int id, String author, String initials, String career, String company,
                String role, String date, String title, String content, int likes, int comments) {
        this.id = id;
        this.author = author;
        this.initials = initials;
        this.career = career;
        this.company = company;
        this.role = role;
        this.date = date;
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.comments = comments;
        this.liked = false;
    }
}
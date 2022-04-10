package com.shanjupay.test.freemarker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * @author old money
 * @create 2022-03-18 15:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Student {

    private String name;

    private int age;

    private Date birthday;

    private Float mondy;

    private List<Student> friends;

    private Student bestFriend;


}

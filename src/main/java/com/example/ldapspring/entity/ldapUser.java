package com.example.ldapspring.entity;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;

import javax.lang.model.element.Name;


@Entry(base = "dc=example,dc=org", objectClasses = {"inetOrgPerson"})
public class ldapUser {

    @Id
    private Name dn;

    @Attribute(name = "uid")
    private String uid;

    @Attribute(name = "cn")
    private String fullName;

    @Attribute(name = "sn")
    private String lastName;

    @Attribute(name = "mail")
    private String email;

    @Attribute(name = "userPassword")
    private String password;



}
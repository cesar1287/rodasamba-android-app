package com.ribeiro.cardoso.rodasamba.data.Entities;

/**
 * Created by diegopc86 on 23/08/14.
 */
public class User {
    public String id;
    public String nome;
    public String sex;
    public int region_id;
    public int age_group_id;
    public String device_os;
    public String device_name;

    @Override
    public String toString() {
        return "id: " + id + "\tnome: " + nome + "\tsex: " + sex + "\tregion: " + region_id + "\tage_group: " + age_group_id + "\tdevice_os: " + device_os + "\tdevice_name: " + device_name;

    }
}

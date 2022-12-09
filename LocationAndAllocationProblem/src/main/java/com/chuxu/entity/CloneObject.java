package com.chuxu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloneObject implements Cloneable{

    List<Candidate> F = new ArrayList<>();
    List<Candidate> F0 = new ArrayList<>();
    List<Candidate> F1 = new ArrayList<>();
    List<Candidate> FF0 = new ArrayList<>();
    List<Candidate> FF1 = new ArrayList<>();
    List<Community> E = new ArrayList<>();
    List<Community> E1 = new ArrayList<>();
    List<Community> EE1 = new ArrayList<>();

    @Override
    public CloneObject clone() {
        try {
            // TODO: 复制此处的可变状态，这样此克隆就不能更改初始克隆的内部
            return (CloneObject) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public CloneObject(List<Candidate> f, List<Candidate> f0, List<Candidate> f1, List<Community> e, List<Community> e1) {
        F = f;
        F0 = f0;
        F1 = f1;
        E = e;
        E1 = e1;
    }
}

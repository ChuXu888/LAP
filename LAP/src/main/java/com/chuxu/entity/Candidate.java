package com.chuxu.entity;

import lombok.Data;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
public class Candidate implements Cloneable,Comparable<Candidate>{
    private Integer id;
    private Double x;
    private Double y;
    private Integer nearestIntersectionId;

    private Double wholeCapacity;  //仅用作记录性的变量，具体操作针对remainCapacity属性执行
    private Double remainCapacity;

    private Set<Integer> slaveCommunityIds;
    private Set<Integer> actualSlaveCommunityIds;  //实际服务哪些居民点，需要结合性质4，5以及分配子算法的结果获得

    private Double wholeLoss;  //若删去该点，其所有以最短距离连接到该备选点的居民点转接到其它备选点上至少会增加的目标值总和，用完要及时清零

    public Candidate() {}

    public Candidate(Integer id, Double x, Double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }


    //重写clone方法
    @Override
    public Candidate clone() {
        try {
            // TODO: 复制此处的可变状态，这样此克隆就不能更改初始克隆的内部
            return (Candidate) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    //id作为唯一标识，只要id相同，就认为它们是同一个备选点fj，主要是hashMap中key为Candidate的实体类时，使用remove(key)，get(key)，时都不方便判定
    //对加入hashmap或hashset中的元素又修改了其属性，由于类默认继承了Object类的hashCode()方法而没有自定义hashcode()方法，导致改了属性之后的同一对象的hash值发生了变化，
    //从而删除时对比hash值对不上。
    //==>解决办法是重写equals方法和hashCode()方法，只要两个备选点的id相同就认为它们指代的是同一个备选点，hash值的生成也只使用id这一个属性，从而其他属性的修改并不会改变hash值
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Candidate candidate = (Candidate) o;
        return id.equals(candidate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Candidate o) {
        return Double.compare(this.wholeLoss,o.wholeLoss);
    }
}

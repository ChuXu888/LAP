package com.chuxu.entity;

import lombok.Data;

import java.util.Objects;
import java.util.Set;

/**
 * 居民点对应的实体类
 * 当需要使用到该实体类的集合，并且对该集合有增删改操作时，就要重写equals()和hashcode()方法
 * @author chuxu
 */
@Data
public class Community implements Cloneable,Comparable<Community>{
    private Integer id;
    private Double x;
    private Double y;
    private Integer populationNum;

    private Integer nearestIntersectionId;

    private Double wholeNeed;  //仅用作记录性的变量，具体操作针对remainCapacity属性执行
    private Double unsatisfiedNeed;

    private Set<Integer> dominatedCandidateIds;
    private Set<Integer> actualDominatedCandidateIds;  //实际被哪些备选点服务，需要结合性质4，5以及分配子算法的结果获得

    private Double lossPerUnit;  //居民点ei的单位需求量转接至其它备选点至少会增加的目标函数值，用完要及时清零

    //用完要及时清零
    private Double minDis;
    private Integer minDisCandidateId;

    private Double minDis2;
    private Integer minDis2CandidateId;

    public Community() {}

    public Community(Integer id, Double x, Double y, Integer populationNum) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.populationNum = populationNum;
    }


    @Override
    public Community clone() {
        try {
            // TODO: 复制此处的可变状态，这样此克隆就不能更改初始克隆的内部
            return (Community) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    //id作为唯一标识，只要id相同，就认为它们是同一个居民点ei
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Community community = (Community) o;
        return id.equals(community.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Community o) {
        return Double.compare(this.lossPerUnit, o.lossPerUnit);
    }

}

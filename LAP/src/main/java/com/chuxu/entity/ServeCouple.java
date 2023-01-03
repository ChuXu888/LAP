package com.chuxu.entity;

import lombok.Data;

import java.util.Objects;

@Data
public class ServeCouple implements Cloneable{

    private Integer communityId;

    private Integer candidateId;

    private Double responsibleNeed;

    private Double distance;

    public ServeCouple(Integer communityId, Integer candidateId, Double responsibleNeed, Double distance) {
        this.communityId = communityId;
        this.candidateId = candidateId;
        this.responsibleNeed = responsibleNeed;
        this.distance = distance;
    }

    @Override
    public ServeCouple clone() {
        try {
            // TODO: 复制此处的可变状态，这样此克隆就不能更改初始克隆的内部
            return (ServeCouple) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServeCouple that = (ServeCouple) o;
        return communityId.equals(that.communityId) && candidateId.equals(that.candidateId) && responsibleNeed.equals(that.responsibleNeed) && distance.equals(that.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(communityId, candidateId, responsibleNeed, distance);
    }

    @Override
    public String toString() {
        return "ServeCouple{" +
                "communityId=" + communityId +
                ", candidateId=" + candidateId +
                ", responsibleNeed=" + responsibleNeed +
                ", distance=" + distance +
                '}';
    }
}

package com.chuxu.entity;

public class Edge {

    public String startName;
    public String endName;
    public Double distance;
    public Double capacity;
    public Double actualFlow;

    public Edge() {
    }

    public Edge(String startName, String endName) {
        this.startName = startName;
        this.endName = endName;
    }

    public Edge(String startName, String endName, Double distance) {
        this.startName = startName;
        this.endName = endName;
        this.distance = distance;
    }

    public Edge(String startName, String endName, Double distance, Double capacity, Double actualFlow) {
        this.startName = startName;
        this.endName = endName;
        this.distance = distance;
        this.capacity = capacity;
        this.actualFlow = actualFlow;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "startName='" + startName + '\'' +
                ", endName='" + endName + '\'' +
                ", distance=" + distance +
                ", capacity=" + capacity +
                ", actualFlow=" + actualFlow +
                '}';
    }
}

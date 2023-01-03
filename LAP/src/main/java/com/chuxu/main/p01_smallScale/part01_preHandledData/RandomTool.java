package com.chuxu.main.p01_smallScale.part01_preHandledData;

import com.chuxu.entity.Candidate;
import com.chuxu.entity.Community;

import java.text.DecimalFormat;
import java.util.*;

public class RandomTool {

    //居民区和备选点之间的距离矩阵【极其重要的一个原始素材，用来创建分配子算法中的边的】
    public static double INF = 10000.0;  //代表不连通
    public static double need = 0.7;  //每人每天所需物资总量
    public static final int candidateNum = 28;
    public static final int communityNum = 42;
    public static final double D = 10.0;  //服务距离上限d
    public static double[][] disMatrix = new double[candidateNum][communityNum];  //距离矩阵
    public static final Random random = new Random();  //随机数工具

    //人口向量
    public static final int[] populationNums = new int[communityNum];

    //容量向量
    public static final double[] capacities = new double[candidateNum];

    //备选点和居民点列表
    public static final List<Community> communities = new ArrayList<>();
    public static final List<Candidate> candidates = new ArrayList<>();

    public static void main(String[] args) {
        createEntity();
        for (double[] matrix : disMatrix) {
            System.out.print("{");
            for (int j = 0; j < disMatrix[0].length; j++) {
                if (j == disMatrix[0].length - 1) {
                    System.out.printf("%8.1f", matrix[j]);
                } else {
                    System.out.printf("%8.1f,", matrix[j]);
                }
            }
            System.out.println("},");
        }
        System.out.println("====================================================");
        System.out.print("{");
        for (int i = 0; i < populationNums.length; i++) {
            if (i == populationNums.length - 1) {
                System.out.print(populationNums[i]);
            } else {
                System.out.print(populationNums[i] + ",");
            }
        }
        System.out.println("};");
        System.out.println("====================================================");
        System.out.print("{");
        for (int i = 0; i < capacities.length; i++) {
            if (i == capacities.length - 1) {
                System.out.print(capacities[i]);
            } else {
                System.out.print(capacities[i] + ",");
            }
        }
        System.out.println("};");
        System.out.println("====================================================");
        candidates.forEach(System.out::println);
        System.out.println("====================================================");
        communities.forEach(System.out::println);
    }

    //2.预处理全过程
    public static void preHandle() throws Exception {
        createEntity();
    }

    //1.创建实体类
    public static void createEntity() {
        DecimalFormat decimalFormat = new DecimalFormat("#.0000");

        for (int i = 0; i < disMatrix.length; i++) {
            for (int j = 0; j < disMatrix[0].length; j++) {
                disMatrix[i][j] = random.nextInt(49) + 1;
            }
        }
        for (int i = 0; i < disMatrix.length; i++) {
            for (int j = 0; j < disMatrix[0].length; j++) {
                if (disMatrix[i][j] > D) {
                    disMatrix[i][j] = INF;
                }
            }
        }

        for (int i = 0; i < populationNums.length; i++) {
            populationNums[i] = random.nextInt(1500) + 500;
        }
        for (int i = 0; i < communityNum; i++) {
            Community community = new Community();
            community.setId(i + 1);
            community.setPopulationNum(populationNums[i]);
            double wholeNeed = Double.parseDouble(decimalFormat.format(populationNums[i] * need));
            community.setWholeNeed(wholeNeed);
            community.setUnsatisfiedNeed(wholeNeed);
            Set<Integer> dominatedCandidateIds = new LinkedHashSet<>();
            for (int j = 0; j < candidateNum; j++) {
                //注意列不变，行变。行代表备选点，列代表居民点
                if (disMatrix[j][i] <= D) {
                    dominatedCandidateIds.add(j + 1);
                }
            }
            community.setDominatedCandidateIds(dominatedCandidateIds);
            communities.add(community);
        }
        
        for (int i = 0; i < capacities.length; i++) {
            capacities[i] = random.nextInt(2000) + 2000;
        }
        for (int i = 0; i < candidateNum; i++) {
            Candidate candidate = new Candidate();
            candidate.setId(i + 1);
            candidate.setWholeCapacity(capacities[i]);
            candidate.setRemainCapacity(capacities[i]);
            Set<Integer> slaveCommunityIds = new LinkedHashSet<>();
            for (int j = 0; j < communityNum; j++) {
                if (disMatrix[i][j] <= D) {
                    slaveCommunityIds.add(j + 1);
                }
            }
            candidate.setSlaveCommunityIds(slaveCommunityIds);
            candidates.add(candidate);
        }
    }
}
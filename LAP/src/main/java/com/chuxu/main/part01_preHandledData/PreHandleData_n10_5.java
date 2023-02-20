package com.chuxu.main.part01_preHandledData;

import com.chuxu.entity.Candidate;
import com.chuxu.entity.Community;

import java.text.DecimalFormat;
import java.util.*;

public class PreHandleData_n10_5 {

    //居民区和备选点之间的距离矩阵【极其重要的一个原始素材，用来创建分配子算法中的边的】
    public static double INF = 10000.0;  //代表不连通
    public static double need = 0.7;  //每人每天所需物资总量
    public static int K = 7;  //选址点数量
    public static final int candidateNum = 10;
    public static final int communityNum = 15;
    public static final double D = 10.0;  //服务距离上限d
    public static double[][] disMatrix = {
            {5.0, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, 6.0, INF, INF, 5.0, INF},
            {INF, INF, 5.0, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, INF, 3.0, INF},
            {INF, INF, 7.0, 3.0, INF, INF, 1.0, 10.0, INF, INF, 8.0, INF, INF, INF, INF},
            {7.0, 6.0, 1.0, INF, INF, 5.0, INF, INF, INF, INF, 4.0, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, 1.0, INF, INF, INF, 5.0, INF, INF, INF, INF, INF},
            {2.0, INF, 10.0, INF, 4.0, INF, INF, 9.0, 7.0, INF, INF, 3.0, INF, INF, 7.0},
            {INF, INF, INF, INF, INF, 2.0, INF, INF, 1.0, INF, 4.0, 9.0, INF, INF, 10.0},
            {INF, 2.0, INF, 8.0, INF, 6.0, INF, 4.0, 4.0, INF, INF, 3.0, 9.0, 1.0, INF},
            {7.0, INF, INF, INF, INF, 10.0, INF, INF, INF, INF, 9.0, INF, 4.0, INF, 7.0},
            {INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
    };

    //人口向量
    public static final int[] populationNums =
            {1970, 1297, 1834, 1398, 1027, 1552, 1270, 1909, 1580, 1385, 1617, 1906, 1525, 1318, 1468};

    //容量向量
    public static final double[] capacities =
            {2034.0, 1940.0, 2747.0, 1749.0, 2834.0, 2972.0, 3372.0, 1949.0, 3486.0, 3342.0};

    //备选点和居民点列表
    public static final List<Community> communities = new ArrayList<>();
    public static final List<Candidate> candidates = new ArrayList<>();

    public static void main(String[] args) {
        createEntity();
        for (double[] matrix : disMatrix) {
            System.out.println(Arrays.toString(matrix));
        }
        System.out.println("====================================================");
        System.out.println(Arrays.toString(populationNums));
        System.out.println("====================================================");
        System.out.println(Arrays.toString(capacities));
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

        //有哪些东西需要随机生成？
        //①距离矩阵：先生成0-30之间的保留的整数，这样每个备选点会服务到1/3的居民点，然后再处理一下，将<D的保持不变，大于D的变为INF；
        //②居民点的人口数量向量：生成500-2000之间的整数；平均1250*0.7=875*15=13125\10=1312.5==>备选点的最小平均容量；
        //③备选点的容量向量：生成1000-2500之间的随机整数，试试效果

        //①距离矩阵：先生成0-30之间的保留的整数，这样每个备选点会服务到1/3的居民点，然后再处理一下，将<D的保持不变，大于D的变为INF；
        //1/3的比例似乎太大了，改为1/5吧

        //②居民点的人口数量向量：生成500-2000之间的整数；平均1250*0.7=875*15=13125\10=1312.5==>备选点的最小平均容量；
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

        //③备选点的容量向量：生成1000-2500之间的随机整数，试试效果
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
package com.chuxu.main.p01_smallScale;

import com.chuxu.entity.*;
import com.chuxu.main.p01_smallScale.part01_preHandledData.*;
import com.chuxu.main.p01_smallScale.part02_properties.Properties;
import com.chuxu.main.p01_smallScale.part03_allocationAlgorithm.AllocationAlgorithm;
import com.chuxu.main.p01_smallScale.part04_upperBoundAlgorithm.UpperBoundAlgorithm;
import com.chuxu.main.p01_smallScale.part06_orderReductionAlgorithm.OrderReductionAlgorithm;
import com.chuxu.main.p01_smallScale.part07_backTrackingAlgorithm.BackTrackingAlgorithm;

import java.util.*;

public class MainAlgorithm {

    public static final double[][] disMatrix = PreHandleData01.disMatrix;  //居民区和备选点之间的距离矩阵
    public static final int K = PreHandleData01.K;;  //选址数量上限
    public static List<Candidate> F = new ArrayList<>();  //备选点列表，作为变量存储，不对它进行操作
    public static List<Community> E = new ArrayList<>();  //居民点列表，作为变量存储，不对它进行操作
    public static List<Candidate> F1 = new ArrayList<>();  //一定开设
    public static List<Candidate> F0 = new ArrayList<>();  //一定不开设
    public static List<Candidate> F5 = new ArrayList<>();  //F5=F\F1\F0
    public static List<Candidate> FF1 = new ArrayList<>();  //假设开设
    public static List<Candidate> FF0 = new ArrayList<>();  //假设不开设
    public static List<Candidate> FF5 = new ArrayList<>();  //FF5=F\F1\F0\FF1\FF0，现在将FF5当作初始不动的F使用，而将原本应该不动的F当作F5和FF5在使用
    public static List<Community> E1 = new ArrayList<>();  //当|FF0∪FF1|=0时，存储降阶时的单度居民点
    public static List<Community> EE1 = new ArrayList<>();  //当|FF0∪FF1|≠0时，存储基于假设时的单度居民点
    public static List<Community> EE5 = new ArrayList<>();  //EE5=E\E1\EE1，现在将EE5当作初始不动的E使用，而将原本应该不动的E当作EE5在使用
    public static double u = Double.MAX_VALUE;  //全局上界
    public static double best_q = Double.MAX_VALUE;  //存储当前最优目标值
    public static Set<Edge> edgesForTrafficOfGlobal = new LinkedHashSet<>();  //存储最优解对应的流量分配方案，如果回溯算法中没有更新过最优解，那回溯结束时它仍为空
    public static double fixedValue = 0.0;  //存储降阶中固定下来的那部分目标值
    public static List<Candidate> F_best = new ArrayList<>();  //当前状态下已知最优目标值对应的开设设施集合
    public static List<ServeCouple> S = new ArrayList<>();  //服务集
    public static int count = 0;  //二叉树搜索次数(包含根节点)
    public static int countOfLeaf = 0;  //二叉树叶子节点搜索次数
    public static int countOfProperty05 = 0;  //二叉树搜索中性质5使用次数
    public static int countOfProperty07 = 0;  //二叉树搜索中性质5使用次数

    public static void main(String[] args) throws Exception {

        //0.数据预处理
        PreHandleData01.preHandle();
        //未做深拷贝之前，F和FF5均指向预处理preHandle中生成的那个candidate列表，所以当我把F清空时，preHandle中的candidates和F、FF5全部都清空了
        //所以现在需要对F、FF5、E、EE5做深拷贝
        deepCopy();

        //1：由性质2、性质3判断该问题是否无解；
        long start = System.currentTimeMillis();
        if (!Properties.property02(F, E)) {
            System.out.println("由于存在孤立的居民点，该问题无解！");
        }
        if (!Properties.property03(F, E)) {
            System.out.println("由于备选点总容量小于居民点总需求量，该问题无解！");
        }
        System.out.println("该问题有解！");

        //2：判断G=(V,X)是否满足性质6的条件，若满足则直接得到最优解，主算法结束；
        List<ServeCouple> couplesOfProperty06 = Properties.property06(F, E);
        if (couplesOfProperty06 != null && !couplesOfProperty06.isEmpty()) {
            System.out.println("========================================================================");
            System.out.println("直接得到了最优解");
            //将当前服务集合加上全局的S就组成了所有的服务集合
            couplesOfProperty06.addAll(S);
            System.out.println("通过性质6得到的服务集合为：");
            couplesOfProperty06.forEach(System.out::println);
        }
        System.out.println("无法直接得到最优解，进入降阶回溯算法的求解过程！");

        //3.上界子算法
        UpperBoundAlgorithm.upperBoundAlgorithm();
        System.out.println("u = " + u);
        printAll();

        //4.降阶子算法
        int F0_Add_F1_size = F0.size() + F1.size();
        OrderReductionAlgorithm.orderReductionAlgorithm();
        int F0_Add_F1_size_new = F0.size() + F1.size();
        printAll();
        System.out.println("fixedValue = " + fixedValue);
        System.out.println("u = " + u);

        //5.若降阶子算法结束后(F0∪F1)发生变化，则重新计算上界【这里可能要用一个while循环】
        //Step7：若降阶子算法结束后(F0∪F1)发生变化，则重新计算上界
        while (F0_Add_F1_size_new != F0_Add_F1_size) {
            //将之前的F0_Add_F1_size_new赋给F0_Add_F1_size，
            //后续会将重新执行上界+降阶后的F0.size() + F1.size()重新赋给F0_Add_F1_size_new
            F0_Add_F1_size = F0_Add_F1_size_new;
            //重新调用上界子算法
            UpperBoundAlgorithm.upperBoundAlgorithm();
            System.out.println("=================================================");
            System.out.println("u = " + u);
            //重新调用降阶子算法
            OrderReductionAlgorithm.orderReductionAlgorithm();
            //更新F0_Add_F1_size_new
            F0_Add_F1_size_new = F0.size() + F1.size();
        }

        //6.调用回溯子算法Backtrack()
        //降阶完成后，若|F1|=K，进入分配子算法看是否可行，若可行则为唯一解也即最优解；
        //若|F1|>K无解；若|F1|<K，进入回溯子算法
        if (F1.size() < K) {
            //当前的F就是F5=F\F0\F1，当前的E就是E\E1
            BackTrackingAlgorithm.backTrackingAlgorithm();
            //回溯过程中不会改变F0和F1，所以也可以在最后看降阶效果
            System.out.println("=======================================================");
            System.out.println("F1.size() = " + F1.size());
            System.out.println("降阶后F1 = ");
            F1.forEach(System.out::println);
            System.out.println("降阶后F0 = ");
            F0.forEach(System.out::println);
            System.out.println("二叉树搜索次数为 = " + count);
            System.out.println("二叉树叶子节点搜索次数为 = " + countOfLeaf);
            System.out.println("二叉树搜索中性质5使用次数 = " + countOfProperty05);
            System.out.println("二叉树搜索中性质7使用次数 = " + countOfProperty07);
            System.out.println("=======================================================");
            System.out.println("u = " + u);
            System.out.println("best_q = " + best_q);
            System.out.println("=======================================================");
            System.out.println("最优值对应的开设集合");
            F_best.forEach(System.out::println);
            System.out.println("=======================================================");
            System.out.println("最优值对应的流量分配方案");
            if (!edgesForTrafficOfGlobal.isEmpty()) {
                //若edgesForTrafficOfGlobal不为空，那么它加上降阶时确定下来的S组成了完整的分配方案
                edgesForTrafficOfGlobal.forEach(System.out::println);
                S.forEach(System.out::println);
            } else {
                //但是如果分配子算法每次都无解，或者即使有解也一次都没有更新过，也即上界子算法中的方案就是最优解
                //此时若要获取分配方案，可以把F_best中的备选点和所有居民点传入分配子算法
                //将从FF5中取得和F_best中id对应的崭新的备选点和E传入分配子算法，求得edgesForTraffic
                List<Candidate> newBest = new ArrayList<>();
                for (Candidate candidate : F_best) {
                    newBest.add(FF5.get(candidate.getId() - 1));
                }
                newBest.forEach(System.out::println);
                EE5.forEach(System.out::println);
                AllocationResult allocationResult = AllocationAlgorithm.allocationAlgorithm(EE5, newBest, disMatrix);
                edgesForTrafficOfGlobal = allocationResult.getEdgesForTraffic();
                //这里传入的是崭新的居民点和备选点，所以不需要加上fixedValue
                System.out.println("allocationResult.getObject() = " + allocationResult.getObject());
                edgesForTrafficOfGlobal.forEach(System.out::println);
            }
        } else if (F1.size() == K) {
            //将从FF5中取得和F_best中id对应的崭新的备选点和E传入分配子算法，求得edgesForTraffic
            //你不能把容量残缺的F1跟需求量完整的居民点EE5传入分配子算法的
            List<Candidate> newF1 = new ArrayList<>();
            for (Candidate candidate : F1) {
                newF1.add(FF5.get(candidate.getId() - 1));
            }
            AllocationResult allocationResult = AllocationAlgorithm.allocationAlgorithm(EE5, newF1, disMatrix);
            if (allocationResult.isFlag()) {
                System.out.println("该问题有解");
                System.out.println("allocationResult.getObject() = " + allocationResult.getObject());
            } else {
                System.out.println("该问题无解");
            }
        } else {
            System.out.println("该问题无解");
        }
        long end = System.currentTimeMillis();
        System.out.println("程序运行总时间为：" + (end - start) / 1000.0 + "s");
    }

    public static void printAll() {
        System.out.println("============================================================================");
        System.out.println("打印当前全局变量：");
        System.out.println("F：");
        F.forEach(System.out::println);
        System.out.println("============================================");
        System.out.println("F1：");
        F1.forEach(System.out::println);
        System.out.println("============================================");
        System.out.println("F0：");
        F0.forEach(System.out::println);
        System.out.println("============================================");
        System.out.println("FF1：");
        FF1.forEach(System.out::println);
        System.out.println("============================================");
        System.out.println("FF0：");
        FF0.forEach(System.out::println);
        System.out.println("============================================");
        System.out.println("FF5：");
        FF5.forEach(System.out::println);
        System.out.println("============================================");
        System.out.println("E：");
        E.forEach(System.out::println);
        System.out.println("============================================");
        System.out.println("E1：");
        E1.forEach(System.out::println);
        System.out.println("============================================");
        System.out.println("EE1：");
        EE1.forEach(System.out::println);
        System.out.println("============================================");
        System.out.println("EE5：");
        EE5.forEach(System.out::println);
    }

    public static void deepCopy() {
        List<Candidate> candidates = PreHandleData01.candidates;
        List<Community> communities = PreHandleData01.communities;
        for (Candidate candidate : candidates) {
            F.add(candidate.clone());
            FF5.add(candidate.clone());
        }
        for (Community community : communities) {
            E.add(community.clone());
            EE5.add(community.clone());
        }
    }
}
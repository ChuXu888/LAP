package com.chuxu.main.p01_smallScale.part07_backTrackingAlgorithm;

import com.chuxu.entity.*;
import com.chuxu.main.p01_smallScale.part02_properties.Properties;
import com.chuxu.main.p01_smallScale.part03_allocationAlgorithm.AllocationAlgorithm;
import com.chuxu.main.p01_smallScale.part05_lowerBoundAlgorithm.LowerBoundAlgorithm;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.chuxu.main.p01_smallScale.MainAlgorithm.*;

public class BackTrackingAlgorithm {

    //操纵全局变量故不需要形参
    public static void backTrackingAlgorithm() {
        System.out.println("============================================================================");
        System.out.println("============================================================================");
        System.out.println("现在进入回溯子算法，先看看当前所有结点的情况：");
        printAll();

        //进入回溯算法
        backTracking();
    }

    //剪枝<==>不搜索，不进去，跳过
    public static void backTracking() {
        //二叉树搜索次数
        count++;

        //若|F1∪FF1|>K，直接return。递归的第一个也是最显然的出口，主要是为了
        //防【右子树中通过性质5】一下子确定了多个点加入FF1，从而导致规模超过K
        List<Candidate> curFs = new ArrayList<>();
        curFs.addAll(F1);
        curFs.addAll(FF1);
        curFs = curFs.stream().distinct().collect(Collectors.toList());

        if (curFs.size() > K) {
            return;
        }

        //Step1：若|F1∪FF1|≤K，调用分配子算法对居民点进行分配，若分配子算法有解，则得到可行解Fk=(F1∪FF1)，若Fk的目标函数值z<best_q则更新上界及最优解u=best_q=z，Fbest=Fk
        //只要curFs.size() <= K，就应该给个机会去分配一下试试，看是否满足另外两个硬约束：容量和全覆盖

        //分配结束后，针对当前的几种情况做出几种决策。注意能走到这里必然满足|F1∪FF1|≤K
        //①如果|FF5|=0，都没有待判断的点了，那么无论|F1∪FF1|＜K还是|F1∪FF1|=K，都直接return，这成为了第二个出口；
        //②如果|FF5|≠0且|F1∪FF1|=K，只可以进行右子树的判断，而不能进行左子树的判断，感觉可以把这个判断挪到左子树中用性质2补判断一下，这里就不判断了
        //③如果|FF5|≠0且|F1∪FF1|<K，那肯定可以进行下去

        //之前的思路出了一些问题：应该是要|FF5|=0时，此时所有的结点都确定下来了开或者不开，此时才形成了一个完整的方案，才需要调分配子算法进行分配尝试
        if (F.size() == 0) {
            //叶子节点搜索次数
            countOfLeaf++;
            //|FF5|=0时，此时所有的结点都确定下来了开或者不开，此时才形成了一个完整的方案，才需要调分配子算法进行分配尝试
            AllocationResult allocationResult = AllocationAlgorithm.allocationAlgorithm(E, curFs, disMatrix);
            //若分配子算法有解，则得到可行解Fk=(F1∪FF1)，若Fk的目标函数值z<best_q则更新上界及最优解u=best_q=z，Fbest=Fk

            edgesForTrafficOfGlobal.forEach(System.out::println);
            if (allocationResult.isFlag()) {
                System.out.println("分配子算法有解！");
                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(allocationResult.getObject()));
                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(fixedValue));
                double z = bigDecimal_1.add(bigDecimal_2).doubleValue();

                //判断是否需要更新上界
                if (z < best_q) {
                    //更新best_q和F_best
                    u = best_q = z;
                    F_best.clear();
                    for (Candidate curF : curFs) {
                        F_best.add(curF.clone());
                    }
                    //更新edgesForTraffic
                    //但是如果分配子算法每次都无解，或者即使有解也一次都没有更新过，也即上界子算法中的方案就是最优解
                    //此时若要获取分配方案，可以把F_best中的备选点和所有居民点传入分配子算法
                    //此外：分配子算法中edgesForTraffic.clear();会导致此处edgesForTrafficOfGlobal = allocationResult.getEdgesForTraffic()
                    //也跟着清掉，所以在此处应该new一个新的集合，可以解决增删同步的问题，若要对其中的元素的属性进行修改，就要用深拷贝
                    edgesForTrafficOfGlobal = new LinkedHashSet<>(allocationResult.getEdgesForTraffic());
                }
            }
            return;  //到了叶子节点肯定要回头了
        }

        //程序能走到这里说明FF5.size()>0是成立的，也就是说FF5里还有结点，直接取其第一个结点作为本层二叉树要操作的结点，左右子树均对其进行操作
        Candidate curCandidate = F.get(0);

        //Step2：情况(1)：假设备选点fj开设，FF1=FF1∪{fj}，FF5=FF5\{fj}
        F.remove(curCandidate);
        FF1.add(curCandidate);
        List<Candidate> calculateSizeCandidates = new ArrayList<>();
        calculateSizeCandidates.addAll(F1);
        calculateSizeCandidates.addAll(FF1);
        calculateSizeCandidates = calculateSizeCandidates.stream().distinct().collect(Collectors.toList());
        boolean noSurpassFlag = Properties.property02_c(calculateSizeCandidates);
        //如果数量超了，则不执行Step2而是转至Step3【恢复状态】，然后转至Step4搜索右子树，左子树不搜索了，也就是剪枝了
        //如果数量没超，才能继续Step2的逻辑
        if (noSurpassFlag) {
            double b = LowerBoundAlgorithm.lowerBoundAlgorithm(F, F0, F1, FF0, FF1, E, E1, EE1);
            if (b <= u) {
                backTracking();
            }
        }

        //Step3：返回上一层前执行FF1=FF1\{fj}，FF5= FF5∪{fj}；
        //在添加回FF5的时候，会出现删的时候是从头删的，添回去确实却是添到了尾巴上，而打乱了FF5的顺序，打乱了二叉树判断的顺序
        //所以添回去的时候需要进行一些操作：具体做法是，通过一个临时列表中转一下，先加curCandidate，再加之前的F
//        System.out.println("================================");
//        System.out.println("F加curCandidate到第一位之前");
//        F.forEach(System.out::println);
        List<Candidate> newF1 = new ArrayList<>();
        newF1.add(curCandidate);  //先加curCandidate
        newF1.addAll(F);  //再加之前的F
        F.clear();  //将F清掉
        F.addAll(newF1);  //实质上是将原F换为了将curCandidate加到了第一位的新F
//        System.out.println("================================");
//        System.out.println("F加curCandidate到第一位之后");
//        F.forEach(System.out::println);

//        F.add(curCandidate);
        FF1.remove(curCandidate);

        //Step4：情况(2)：假设备选点fj不开设，FF0=FF0∪{fj}}，FF5=FF5\{fj}
        F.remove(curCandidate);
        FF0.add(curCandidate);

        //①此时无需判断性质2补，因为右子树是假设不在，|F1∪FF1|≤K之前就满足，这里必然也满足
        //②判断此时是否满足性质2和性质3的条件，传入实参为更新之后的F\F0\FF0=F1∪FF1∪FF5(F)，E\E1\EE1
        List<Candidate> F1_Add_FF1_Add_FF5 = new ArrayList<>();
        F1_Add_FF1_Add_FF5.addAll(F1);
        F1_Add_FF1_Add_FF5.addAll(FF1);
        F1_Add_FF1_Add_FF5.addAll(F);
        F1_Add_FF1_Add_FF5 = F1_Add_FF1_Add_FF5.stream().distinct().collect(Collectors.toList());

        //借用一下上面定义的F1_Add_FF1_Add_FF5，用作性质2和性质3的判断
        //由性质2和性质3判断该情况下是否无解，
        //F(FF5)\F0\FF0=F1∪FF1∪FF5(F)，E(EE5)\E1\EE1=EE5(E)
        //如果性质2返回true，性质3也返回true，那就继续进行，如果有一个返回false，则直接进入第五步进行回溯，也即剪枝了，右子树不进去了
        List<Candidate> Ftemp1 = new ArrayList<>();
        List<Candidate> Ftemp2 = new ArrayList<>();
        List<Community> Etemp = new ArrayList<>();
        List<ServeCouple> serveCouplesOfProperty06 = new ArrayList<>();
        LinkedHashMap<Community, Candidate> hashMapOfProperty05 = new LinkedHashMap<>();
        //存储性质7暂时固定下来的目标值，下面要恢复的；如果没有触发性质7，那就以0做个加法减法
        double fixedValue_temp = 0.0;

        //Step4核心：如果不无解。如果无解，则不执行Step4而是转至Step5【恢复状态】
        if (Properties.property02(F1_Add_FF1_Add_FF5, E)) {
            //如果不无解。如果无解，则不执行Step4而是转至Step5【恢复状态】
            if (Properties.property03(F1_Add_FF1_Add_FF5, E)) {
                //要么满足性质6要么不满足性质6，这两条路只能走一条，而且只有不满足性质6才会往后执行Step5，满足性质6有它自己的一套逻辑
                serveCouplesOfProperty06 = Properties.property06(F1_Add_FF1_Add_FF5, E);
                //如果满足性质6
                if (serveCouplesOfProperty06 != null && serveCouplesOfProperty06.size() != 0) {

                    //若满足性质6，也是得到了一个完整方案，也是一个叶子节点
                    countOfLeaf++;

                    double z = fixedValue;
                    for (ServeCouple serveCouple : serveCouplesOfProperty06) {
                        BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(z));
                        BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(serveCouple.getResponsibleNeed() * serveCouple.getDistance()));
                        z = bigDecimal_1.add(bigDecimal_2).doubleValue();
                    }
                    if (z < best_q) {
                        //更新best_q和F_best
                        u = best_q = z;
                        F_best.clear();
                        for (ServeCouple serveCouple : serveCouplesOfProperty06) {
                            F_best.add(FF5.get(serveCouple.getCandidateId() - 1));  //从FF5里面根据id取
                        }
                        F_best = F_best.stream().distinct().collect(Collectors.toList());  //去重
                        //这种情况下其实无需调用分配子算法，更不用谈流量边了，此时serveCouplesOfProperty06与全局变量的S加起来就组成了分配方案
                        System.out.println("================================================================");
                        System.out.println("此时serveCouplesOfProperty06与全局变量的S加起来就组成了分配方案：");
                        serveCouplesOfProperty06.forEach(System.out::println);
                        S.forEach(System.out::println);

//                        //如果已经没有点可以判断了，那肯定直接回头
//                        if (F.size() == 0) {
//                            return;
//                        }
//
//                        //如果还有点可以判断，那么不管|F1∪FF1|＜K还是|F1∪FF1|=K，都可以进入下一层试试看
//                        backTracking();

                        //从if执行完进入Step5恢复状态
//                        //回溯完之后恢复状态，只要有backTracking的地方后面就要有恢复状态的代码
//                        F.add(curCandidate);
//                        FF0.remove(curCandidate);
                    }
                } else {
                    //如果不满足性质6
                    double b = LowerBoundAlgorithm.lowerBoundAlgorithm(F, F0, F1, FF0, FF1, E, E1, EE1);
                    if (b <= u) {
                        //由性质7得出加入FF0中的备选点集合为Ftemp1
                        Ftemp1 = Properties.property07(F1_Add_FF1_Add_FF5, E);
                        if (!Ftemp1.isEmpty()) {
                            countOfProperty08++;
                            //删好友，遍历所有的E\E1\EE1(E)，移除所有的居民点的dominatedCandidateIds属性中的deletedCandidate的id
                            for (Community community : E) {
                                for (Candidate candidate : Ftemp1) {
                                    if (community.getDominatedCandidateIds().contains(candidate.getId())) {
                                        Set<Integer> curDominatedCandidateIds = community.getDominatedCandidateIds();
                                        curDominatedCandidateIds.remove(candidate.getId());
                                        community.setDominatedCandidateIds(curDominatedCandidateIds);
                                    }
                                }
                            }
                            //追加更新
                            F.removeAll(Ftemp1);
                            FF0.addAll(Ftemp1);
                            //性质7可能会导致FF5有更新，所以现在准备一下下面传入性质7的实参为F1_Add_FF1_Add_FF5\Ftemp1，
                            //如果没有进入这个if就不会更新
                            F1_Add_FF1_Add_FF5.removeAll(Ftemp1);
                        }

                        //由性质5得出加入FF1中的备选点集合为Ftemp2，并将对应的居民点加入Etemp
                        //将可能更新过的F1_Add_FF1_Add_FF5和E作为实参传入性质5
                        hashMapOfProperty05 = Properties.property05(F1_Add_FF1_Add_FF5, E);
                        if (!hashMapOfProperty05.isEmpty()) {
                            //遍历性质5的返回结果
                            //这里犯了一个错误：应该首先看hashMapOfProperty05中涉及的备选点是否为FF5中的备选点
                            //只有是FF5中的备选点才能进行任何的数据修改，此处我却先更新了容量，并且Etemp和Ftemp2也更新了，后面发现Ftemp2没有FF5
                            //中的点时也忘了将Etemp清空，导致E.addAll(Etemp);居然执行了，而Ftemp2明明是空的
                            for (Map.Entry<Community, Candidate> entry : hashMapOfProperty05.entrySet()) {
                                Ftemp2.add(entry.getValue());
                            }
                            //对Ftemp2进行去重
                            Ftemp2 = Ftemp2.stream().distinct().collect(Collectors.toList());
                            //注意性质5传入实参为F1_Add_FF1_Add_FF5，所以Ftemp2中的元素可能是在F1、FF1、FF5内
                            //而只有当Ftemp2在FF5中时才是真正的剪枝
                            //而如果Ftemp2中的元素全部为F1或者FF1或者两个地方都有的话，那就不需要追加更新
                            //要从Ftemp2中取出在FF5中的元素，Ftemp2∩FF5之后的才需要从FF5中移除，也即真正的降阶了
                            Ftemp2 = Ftemp2.stream().filter(item -> F.contains(item)).collect(Collectors.toList());
                            Ftemp2.forEach(System.out::println);

                            //如果Ftemp2中的点不在FF5中，那么其实就没有达到降阶的效果，那我还不如什么都不做
                            if (!Ftemp2.isEmpty()) {
                                //如果Ftemp2不为空，再从hashMapOfProperty05寻找Ftemp2中还剩下的属于FF5中的备选点及其搭档
                                for (Candidate candidate : Ftemp2) {
                                    for (Map.Entry<Community, Candidate> entry : hashMapOfProperty05.entrySet()) {
                                        if (entry.getValue().getId().equals(candidate.getId())) {
                                            //当前涉及到居民点，它们和<F1_Add_FF1_Add_FF5,E>指向的是同一个对象，【一改具改】
                                            Community community = entry.getKey();
                                            //计算更新后的容量
                                            //正是因为candidate以及F1_Add_FF1_Add_FF5以及下面的varyCandidate本质上指向的是同一个对象，所以下面重新setRemainCapacity，而这里又getRemainCapacity才能保证是最新的
                                            BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(candidate.getRemainCapacity()));
                                            BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(community.getUnsatisfiedNeed()));
                                            double newRemainCapacity = bigDecimal_1.subtract(bigDecimal_2).doubleValue();

                                            //既然三个引用指向同一个对象等会我直接设置curCandidate的【剩余容量】不也行么？
                                            candidate.setRemainCapacity(newRemainCapacity);

                                            //更新Etemp和Ftemp2
                                            Etemp.add(entry.getKey());

                                            //计算当前配对之间的距离
                                            int rowIndex = candidate.getId() - 1;
                                            int columnIndex = community.getId() - 1;
                                            double curDis = disMatrix[rowIndex][columnIndex];

                                            //计算性质5中临时产生的固定下来的目标值
                                            BigDecimal bigDecimal_3 = new BigDecimal(Double.toString(fixedValue_temp));
                                            BigDecimal bigDecimal_4 = new BigDecimal(Double.toString(curDis * community.getUnsatisfiedNeed()));
                                            fixedValue_temp = bigDecimal_3.add(bigDecimal_4).doubleValue();
                                        }
                                    }
                                }
                                countOfProperty06++;
                                //追加更新
                                F.removeAll(Ftemp2);
                                FF1.addAll(Ftemp2);
                                E.removeAll(Etemp);
                                EE1.addAll(Etemp);
                                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(fixedValue));
                                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(fixedValue_temp));
                                fixedValue = bigDecimal_1.add(bigDecimal_2).doubleValue();
                            }
                        }
                        //从else分支的b<=u分支执行完之后，进入Step5恢复状态
                        backTracking();
                    }
                }
            }
        }

        //Step5：返回上一层前执行FF0=FF0\{fj}\Ftemp1，FF1=FF1\Ftemp2，FF5=FF5∪{fj}∪Ftemp1∪Ftemp2，EE1=EE1\Etemp。
        EE1.removeAll(Etemp);
        E.addAll(Etemp);
        FF0.remove(curCandidate);
//        F.add(curCandidate);

//        System.out.println("================================");
//        System.out.println("F加curCandidate到第一位之前");
//        F.forEach(System.out::println);
        List<Candidate> newF2 = new ArrayList<>();
        newF2.add(curCandidate);  //先加curCandidate
        newF2.addAll(F);  //再加之前的F
        F.clear();  //将F清掉
        F.addAll(newF2);  //实质上是将原F换为了将curCandidate加到了第一位的新F
//        System.out.println("================================");
//        System.out.println("F加curCandidate到第一位之后");
//        F.forEach(System.out::println);

        FF0.removeAll(Ftemp1);
        F.addAll(Ftemp1);
        FF1.removeAll(Ftemp2);
        F.addAll(Ftemp2);

        //如果Ftemp1不为空，就还要把删除的好友加回来
        if (!Ftemp1.isEmpty()) {
            for (Candidate candidate : Ftemp1) {
                //EE5中含有完整邻接点信息
                for (Community community1 : EE5) {
                    for (Community community2 : E) {
                        //如果当前E中的居民点和EE5中居民点id对上了，并且EE5中该居民点有当前备选点的好友而E中的没有，那就重新把好友加上
                        if (community2.getId().equals(community1.getId())
                                && community1.getDominatedCandidateIds().contains(candidate.getId())
                                && !community2.getDominatedCandidateIds().contains(candidate.getId())) {
                            Set<Integer> curDominatedCandidateIds = community2.getDominatedCandidateIds();
                            curDominatedCandidateIds.add(candidate.getId());
                            community2.setDominatedCandidateIds(curDominatedCandidateIds);
                        }
                    }
                }
            }
        }
        //如果Ftemp2不为空，则要把之前减去的容量加回来【这里不使用记忆功能是因为我需要全局修改，或者说我是要浅拷贝，要多个引用指向同一个对象，一改具改】；
        //同时把之前临时固定下来的目标值也要减掉
        if (!Ftemp2.isEmpty()) {
            for (Map.Entry<Community, Candidate> entry : hashMapOfProperty05.entrySet()) {
                //当前涉及到居民点和备选点，它们和<F1_Add_FF1_Add_FF5,E>指向的是同一个对象，【一改具改】
                Community community = entry.getKey();
                Candidate candidate = entry.getValue();

                //计算更新后的容量
                //正是因为candidate以及F1_Add_FF1_Add_FF5以及下面的varyCandidate本质上指向的是同一个对象，所以下面重新setRemainCapacity，而这里又getRemainCapacity才能保证是最新的
                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(candidate.getRemainCapacity()));
                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(community.getUnsatisfiedNeed()));
                double newRemainCapacity = bigDecimal_1.add(bigDecimal_2).doubleValue();

                //既然三个引用指向同一个对象等会我直接设置curCandidate的【剩余容量】不也行么？
                candidate.setRemainCapacity(newRemainCapacity);
            }

            //同时把之前临时固定下来的目标值也要减掉
            BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(fixedValue));
            BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(fixedValue_temp));
            fixedValue = bigDecimal_1.subtract(bigDecimal_2).doubleValue();
        }
    }
}
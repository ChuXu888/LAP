package com.chuxu.main.p01_smallScale.part06_orderReductionAlgorithm;


import com.chuxu.entity.Candidate;
import com.chuxu.entity.Community;
import com.chuxu.entity.ServeCouple;
import com.chuxu.main.p01_smallScale.MainAlgorithm;
import com.chuxu.main.p01_smallScale.part02_properties.Properties;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.chuxu.main.p01_smallScale.MainAlgorithm.*;

//降阶子算法直接对全局变量进行操作，应该就不用做深拷贝了
public class OrderReductionAlgorithm {


    public static void main(String[] args) {

    }

    public static void orderReductionAlgorithm() {
        System.out.println("============================================================================");
        System.out.println("============================================================================");
        System.out.println("现在进入降阶子算法，先看看当前所有结点的情况：");
        printAll();

        //Step1：利用性质9对问题进行降阶；
        List<Candidate> deleteCandidates = Properties.property09(F, F0, F1, E, E1);
        System.out.println("deleteCandidates.size() = " + deleteCandidates.size());
        if (!deleteCandidates.isEmpty()) {
            System.out.println("========================================================================");
            System.out.println("由性质9确定的一定不开设的备选点为：");
            deleteCandidates.forEach(System.out::println);
            F.removeAll(deleteCandidates);
            F0.addAll(deleteCandidates);
            System.out.println("============================================================================");
            System.out.println("经过性质9处理后所有结点的情况：");
            printAll();

            //Step2：若Step1处理后F0发生变化【其实就是在这个if里面判断】，则由性质2和性质3判断该问题是否无解，由性质6判断是否能直接得到该问题的最优解；
            //由于上界子算法可能会二次调用，所以也要考虑到F1不为空的情况，所以还是应该用F\F0=F1∪F5(F)
            List<Candidate> F1_Add_F5 = new ArrayList<>();
            F1_Add_F5.addAll(F);
            F1_Add_F5.addAll(F1);
            if (!Properties.property02(F1_Add_F5, E)) {
                System.out.println("该问题无可行解！");
                System.exit(0);
            }
            if (!Properties.property03(F1_Add_F5, E)) {
                System.out.println("该问题无可行解！");
                System.exit(0);
            }

            //由性质6判断是否能直接得到该问题的最优解
            List<ServeCouple> couplesOfProperty06 = Properties.property06(F1_Add_F5, E);
            if (couplesOfProperty06 != null && !couplesOfProperty06.isEmpty()) {
                System.out.println("========================================================================");
                System.out.println("直接得到了最优解");
                //将当前服务集合加上全局的S就组成了所有的服务集合
                couplesOfProperty06.addAll(S);
                System.out.println("通过性质6得到的服务集合为：");
                couplesOfProperty06.forEach(System.out::println);
            }

            //删好友环节，删除E\E1的dominatedCandidateIds属性中的包含有deleteCandidates的id
            //遍历所有的E\E1=E，其实加上E1也可，但也不是特别必要【如果产生了E1会及时更新掉的，所以这里遍历E即可】，移除所有的居民点的dominatedCandidateIds属性中的deletedCandidate的id
            for (Community community : E) {
                for (Candidate candidate : deleteCandidates) {
                    if (community.getDominatedCandidateIds().contains(candidate.getId())) {
                        Set<Integer> curDominatedCandidateIds = community.getDominatedCandidateIds();
                        curDominatedCandidateIds.remove(candidate.getId());
                        community.setDominatedCandidateIds(curDominatedCandidateIds);
                    }
                }
            }

            //由于删除了一些结点，那么就有可能形成新的单度点，所以再次调用性质4
            System.out.println("============================================================================");
            System.out.println("开始性质4的判断");
            LinkedHashMap<Community, Candidate> singleCommunityCandidate = Properties.property04(F1_Add_F5, E);
            //对性质4的返回做非空判断，如果不为空才会进入后续的处理逻辑，由于性质内部都是new一个对象来返回的，所以不会为null，只用判断其size是否为0即可
            if (!singleCommunityCandidate.isEmpty()) {
                for (Map.Entry<Community, Candidate> entry : singleCommunityCandidate.entrySet()) {
                    System.out.println(entry);
                    Community curCommunity = entry.getKey();
                    Candidate curCandidate = entry.getValue();
                    //计算更新后的容量
                    BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(curCandidate.getRemainCapacity()));
                    BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(curCommunity.getWholeNeed()));
                    double newRemainCapacity = bigDecimal_1.subtract(bigDecimal_2).doubleValue();

                    //既然三个引用指向同一个对象等会我直接设置curCandidate的剩余容量不也行么？
                    curCandidate.setRemainCapacity(newRemainCapacity);

                    //将fj并加入集合F1
                    F1.add(curCandidate);
                    //将ei加入E1
                    E1.add(curCommunity);

                    //计算当前eiAndfj_couple的锁定下来的目标值加到best_q上去
                    int rowIndex = curCandidate.getId() - 1;
                    int columnIndex = curCommunity.getId() - 1;
                    double curDis = disMatrix[rowIndex][columnIndex];
                    BigDecimal bigDecimal_3 = new BigDecimal(Double.toString(fixedValue));
                    BigDecimal bigDecimal_4 = new BigDecimal(Double.toString(curDis * curCommunity.getUnsatisfiedNeed()));
                    fixedValue = bigDecimal_3.add(bigDecimal_4).doubleValue();

                    //将ei,fj,ei的需求量,服务距离，加入服务集S中
                    S.add(new ServeCouple(curCommunity.getId(), curCandidate.getId(), curCommunity.getWholeNeed(), curDis));
                }

                //打印S看看咯
                MainAlgorithm.S.forEach(System.out::println);
                //循环结束后，先使用Stream流优雅地对F1去重，
                F1 = F1.stream().distinct().collect(Collectors.toList());

                //由性质2补判断该问题是否无解
                if (!Properties.property02_c(F1)) {
                    System.out.println("该问题无可行解！");
                    System.exit(0);
                }

                //性质4的后续判断：循环结束后，查看F1中元素的剩余容量是否都不小于0，如果是，那就可以继续；否则该问题无解。
                for (Candidate candidate : F1) {
                    if (candidate.getRemainCapacity() < 0) {
                        System.out.println("单度居民点对应的备选点容量不够，该问题无解！");
                        System.exit(0);
                    }
                }

                //一次性执行F=F\F1，E=E\E1，而不是在循环里面发现一个弄一个
                F.removeAll(F1);
                E.removeAll(E1);
            }

    }

        //Step3：利用性质10和性质11对问题进行降阶；
        List<Candidate> openCandidatesOfProperty10 = Properties.property10(F, F0, F1, E, E1);
        System.out.println("openCandidatesOfProperty10.size() = " + openCandidatesOfProperty10.size());
        List<Candidate> F1_Add_F5 = new ArrayList<>();
        F1_Add_F5.addAll(F1);
        F1_Add_F5.addAll(F);
        F1_Add_F5 = F1_Add_F5.stream().distinct().collect(Collectors.toList());
        List<Candidate> openCandidatesOfProperty11 = Properties.property11(F1_Add_F5, E);  //计算F\F0=F1∪F5(F)，E\E1=E
        System.out.println("openCandidatesOfProperty11.size() = " + openCandidatesOfProperty11.size());
        //计算性质10和性质11确定的必须开设的集合的规模
        List<Candidate> openCandidatesOfProperty1011 = new ArrayList<>();
        openCandidatesOfProperty1011.addAll(openCandidatesOfProperty10);
        openCandidatesOfProperty1011.addAll(openCandidatesOfProperty11);
        //去重
        openCandidatesOfProperty1011 = openCandidatesOfProperty1011.stream().distinct().collect(Collectors.toList());
        System.out.println("openCandidatesOfProperty1011.size() = " + openCandidatesOfProperty1011.size());
        if (!openCandidatesOfProperty1011.isEmpty()) {
            System.out.println("========================================================================");
            System.out.println("由性质10和性质11共同确定的一定开设的备选点为：");
            openCandidatesOfProperty1011.forEach(System.out::println);
            //更新集合
            F.removeAll(openCandidatesOfProperty1011);
            F1.addAll(openCandidatesOfProperty1011);

            //Step4：若Step3处理后F1发生变化，则由性质2补判断该问题是否无解；
            if (!Properties.property02_c(F1)) {
                System.out.println("该问题无可行解！");
                System.exit(0);
            }
        }

        //打印所有看看情况，当前案例中，已经通过降阶得到了开设的4个备选点
        printAll();

        //Step5：利用性质8对问题进行降阶，其本质是互删好友，此处不进行深拷贝，方法内部直接对该对象进行修改操作
        //形参F\F0\FF0和EE5，此时FF0为空，EE1也为空，F\F0\=F1∪F5(F)，E\E1=E
        List<Candidate> F1_Add_F5_AfterProperty1011 = new ArrayList<>();
        F1_Add_F5_AfterProperty1011.addAll(F);
        F1_Add_F5_AfterProperty1011.addAll(F1);
        Properties.property08(F1_Add_F5_AfterProperty1011, E);
        System.out.println("========================================================================");
        System.out.println("降阶子算法结束后的全局变量情况：");
    }
}
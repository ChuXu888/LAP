package com.chuxu.main.p01_smallScale.part04_upperBoundAlgorithm;

import com.chuxu.entity.Candidate;
import com.chuxu.entity.CloneObject;
import com.chuxu.entity.Community;
import com.chuxu.entity.ServeCouple;
import com.chuxu.main.p01_smallScale.MainAlgorithm;
import com.chuxu.main.p01_smallScale.part02_properties.Properties;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.chuxu.main.p01_smallScale.MainAlgorithm.*;

public class UpperBoundAlgorithm {

    private static final double u_limit = 1000000000.0;

    public static void main(String[] args) {

    }

    public static void upperBoundAlgorithm() {
        System.out.println("============================================================================");
        System.out.println("============================================================================");
        System.out.println("现在进入上界子算法");

        //由于可能会重复调用上界子算法，所以上界子算法一开始就要将u和best_q置为0.0然后从头开始，
        //best_q保存降阶时固定下来的目标值，每次都要从0开始算，然后赋给全局变量
        //u为全局上界，每次重新算法一个，然后跟之前的全局上界做比较，看谁比较大
        //best_q存储降阶中固定下来的那部分目标值，第一次调用为0，第二次调用就是第一次调用结束之后的值了
        //当然也可以再单独弄个变量
        double cur_u = fixedValue;

        //注意：无论是全局降阶还是局部降阶，都最好克隆之后再进行操作，以免造成一些不必要的麻烦
        CloneObject cloneObject = cloneObject(F, F0, F1, E, E1);
        List<Candidate> F = cloneObject.getF();
        List<Candidate> F0 = cloneObject.getF0();
        List<Candidate> F1 = cloneObject.getF1();
        List<Community> E = cloneObject.getE();
        List<Community> E1 = cloneObject.getE1();

        //定义传入性质7中的参数，现在是降阶阶段，所以没有FF0和FF1和EE1的概念，所以计算F\F0和E\E1即可
        //计算F\F0，计算E\E1
        F.removeAll(F0);
        E.removeAll(E1);

        //1.由性质7找出一定不开设的备选点并加入集合F0；
        System.out.println("============================================================================");
        System.out.println("开始性质7的判断");
        List<Candidate> deletedCandidates = Properties.property07(F, E);
        deletedCandidates = deletedCandidates.stream().distinct().collect(Collectors.toList());

        //对性质7的返回做非空判断，如果不为空才会进入后续的处理逻辑，由于性质内部都是new一个对象来返回的，所以不会为null，只用判断其size是否为0即可
        if (!deletedCandidates.isEmpty()) {
            //全局降阶：故从F中移除deletedCandidate
            F.removeAll(deletedCandidates);
            //将deletedCandidate加入F0
            F0.addAll(deletedCandidates);
            //遍历所有的E\E1【如果产生了E1会及时更新掉的，所以这里遍历E即可】，移除所有的居民点的dominatedCandidateIds属性中的deletedCandidate的id
            for (Community community : E) {
                for (Candidate candidate : deletedCandidates) {
                    if (community.getDominatedCandidateIds().contains(candidate.getId())) {
                        Set<Integer> curDominatedCandidateIds = community.getDominatedCandidateIds();
                        curDominatedCandidateIds.remove(candidate.getId());
                        community.setDominatedCandidateIds(curDominatedCandidateIds);
                    }
                }
            }
            //处理完毕后打印看看结果
            System.out.println("=================================================");
            System.out.println("经过性质7处理，有部分结点加入F0之后的F5(F)：");
            F.forEach(System.out::println);
            System.out.println("=================================================");
            System.out.println("经过性质7处理后的F0：");
            F0.forEach(System.out::println);
            System.out.println("=================================================");
            System.out.println("经过性质7处理后，将E中所有居民点的邻接备选点中涉及到F0中元素的备选点都删了：");
            E.forEach(System.out::println);
            System.out.println("========================================================================");
        }

        //由性质2和性质3判断该问题是否无解，现在的F其实就是F5=F\F0\F1，且此时F1为空，所以F=F\F0即为第一个参数，E=E\E1为第二个参数
        //由于上界子算法可能会二次调用，所以也要考虑到F1不为空的情况，所以还是应该用F\F0=F1∪F5(F)
        List<Candidate> F1_Add_F5 = new ArrayList<>();
        F1_Add_F5.addAll(F);
        F1_Add_F5.addAll(F1);
        System.out.println("============================================================================");
        System.out.println("开始性质2的判断");
        if (!Properties.property02(F1_Add_F5, E)) {
            System.out.println("该问题无可行解！");
            System.exit(0);
        }
        System.out.println("============================================================================");
        System.out.println("开始性质3的判断");
        if (!Properties.property03(F1_Add_F5, E)) {
            System.out.println("该问题无可行解！");
            System.exit(0);
        }

        //由性质6判断是否能直接得到该问题的最优解
        System.out.println("============================================================================");
        System.out.println("开始性质6的判断");
        List<ServeCouple> couplesOfProperty06 = Properties.property06(F1_Add_F5, E);
        if (couplesOfProperty06 != null && !couplesOfProperty06.isEmpty()) {
            System.out.println("========================================================================");
            System.out.println("直接得到了最优解");
            //将当前服务集合加上全局的S就组成了所有的服务集合
            couplesOfProperty06.addAll(S);
            System.out.println("通过性质6得到的服务集合为：");
            couplesOfProperty06.forEach(System.out::println);
        }

        //2.由性质4找出所有一定开设的备选点fj并加入集合F1，更新将对应的居民点加入E1，
        //更新F1中各备选点的剩余容量rj，并执行best_q=best_q+E1和F1中配对的相应目标值
        //将经过性质7处理后的F\F0和E\E1传入性质4
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

                //计算当前eiAndfj_couple的锁定下来的目标值加到fixedValue上去
                int rowIndex = curCandidate.getId() - 1;
                int columnIndex = curCommunity.getId() - 1;
                double curDis = disMatrix[rowIndex][columnIndex];
                BigDecimal bigDecimal_3 = new BigDecimal(Double.toString(fixedValue));
                BigDecimal bigDecimal_4 = new BigDecimal(Double.toString(curDis * curCommunity.getUnsatisfiedNeed()));
                fixedValue = bigDecimal_3.add(bigDecimal_4).doubleValue();
                cur_u = fixedValue;  //用fixedValue存储降阶时固定下来的这一部分，也要加到u上面来的，然后u继续计算，而fixedValue则到此为止了

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
                    F1.forEach(System.out::println);
                    System.exit(0);
                }
            }

            //一次性执行F=F\F1，E=E\E1，而不是在循环里面发现一个弄一个
            F.removeAll(F1);
            E.removeAll(E1);

            //处理完毕后打印看看结果
            System.out.println("=================================================");
            System.out.println("从这里开始，上界子算法的全局降阶过程就结束了，此时将经过改变的F5(F)、F1、F0、E-E1(E)、E1拷贝给主函数中的对应变量：");
            System.out.println("=================================================");
            System.out.println("经过性质4处理后的F5(F)：");
            F.forEach(System.out::println);
            //将上界子算法中的F拷贝给主函数中的F
            MainAlgorithm.F.clear();
            for (Candidate candidate : F) {
                MainAlgorithm.F.add(candidate.clone());
            }
            System.out.println("反拷贝结束后：");
            MainAlgorithm.F.forEach(System.out::println);
            System.out.println("=================================================");
            System.out.println("经过性质4处理后的F1：");
            F1.forEach(System.out::println);
            //将上界子算法中的F拷贝给主函数中的F1
            MainAlgorithm.F1.clear();
            for (Candidate candidate : F1) {
                MainAlgorithm.F1.add(candidate.clone());
            }
            System.out.println("反拷贝结束后：");
            MainAlgorithm.F1.forEach(System.out::println);
            System.out.println("=================================================");
//            System.out.println("经过性质4处理后的F0：");
//            F0.forEach(System.out::println);
            //将上界子算法中的F拷贝给主函数中的F1
            MainAlgorithm.F0.clear();
            for (Candidate candidate : F0) {
                MainAlgorithm.F0.add(candidate.clone());
            }
//            System.out.println("反拷贝结束后：");
//            MainAlgorithm_SmallScale.F0.forEach(System.out::println);
            System.out.println("=================================================");
//            System.out.println("经过性质4处理后的E(E-E1)：");
//            E.forEach(System.out::println);
            MainAlgorithm.E.clear();
            for (Community community : E) {
                MainAlgorithm.E.add(community.clone());
            }
//            System.out.println("反拷贝结束后：");
//            MainAlgorithm_SmallScale.E.forEach(System.out::println);
            System.out.println("=================================================");
//            System.out.println("经过性质4处理后的E1：");
//            E1.forEach(System.out::println);
            MainAlgorithm.E1.clear();
            for (Community community : E1) {
                MainAlgorithm.E1.add(community.clone());
            }
//            System.out.println("反拷贝结束后：");
//            MainAlgorithm_SmallScale.E1.forEach(System.out::println);
            System.out.println("=================================================");
            System.out.println("经过性质4处理后的主函数中的S：");
            S.forEach(System.out::println);
            System.out.println("========================================================================");
            System.out.println("经过性质4处理后的当前最佳的fixedValue = " + fixedValue);
        }

        //3.初始化Fup=F1，定义一个变量存储E\E1的结果，由于上面E中已经减去了E1，所以现在存储的E其实就是E\E1的结果
        //从这里开始，后面的都不能操纵全局变量了，因为后面对F，E等的操作都是上界子算法自己的内部的操作，不能上升到全局上面去，
        //需要克隆一份进行操作
        List<Candidate> F_up = new ArrayList<>();
        for (Candidate candidate : F1) {
            F_up.add(candidate.clone());
        }
        List<Community> E_Minus_E1 = new ArrayList<>();
        for (Community community : E) {
            E_Minus_E1.add(community.clone());
        }
        System.out.println("=================================================");
        System.out.println("F_up：");
        F_up.forEach(System.out::println);
        System.out.println("=================================================");
        System.out.println("E(E-E1)：");
        E_Minus_E1.forEach(System.out::println);


        //4.依次获取E\E1中各居民点ei的min_d(G[F\F0], ei)对应的备选点fj并将其加入集合Fup；
        // 现在的F实际上是F5，F\F0=F1∪F5，所以将此时的F和F1合并起来就是F\F0

        //hashMap中的candidate来源于minDisCommunityCandidate，又来源于F1_Add_F5_AfterProperty04，又来源于F1
        //Fup中的candidate来源于F1，但是现在要保持hashMap中的candidate和Fup中的candidate的一致性，
        //就要让它们其中一个浅拷贝自另一个，而不能都从F1深拷贝，这样它们就没有什么联系了
        //由于F1_Add_F5_AfterProperty04比Fup的初始范围大，所以可以让F1_Add_F5_AfterProperty04先浅拷贝自Fup，然后再添加F5(F)
        //第一次不受影响的原因是：minDisCommunityCandidate刚好没有涉及到F1中的元素，所以容量更新也更新不到它们头上，
        //所以虽然Fup中f4和f6也是深拷贝的，但是后面的没有涉及到它们，所以看似没有影响。如果涉及到了，那就也会影响
        List<Candidate> F1_Add_F5_AfterProperty04 = new ArrayList<>(F_up);
        F1_Add_F5_AfterProperty04.addAll(F);

//        List<Candidate> F1_Add_F5_AfterProperty04 = new ArrayList<>();
//        for (Candidate candidate : F1) {
//            F1_Add_F5_AfterProperty04.add(candidate.clone());
//        }
//        for (Candidate candidate : F) {
//            F1_Add_F5_AfterProperty04.add(candidate.clone());
//        }

        System.out.println("=================================================");
        System.out.println("F1∪F5：");
        F1_Add_F5_AfterProperty04.forEach(System.out::println);

        //存储各居民点的最短距离备选点之间的键值对
        LinkedHashMap<Community, Candidate> minDisCommunityCandidate = new LinkedHashMap<>();
        for (Community community : E_Minus_E1) {
            double minDis = Double.MAX_VALUE;
            Candidate minDisCandidate = null;
            for (Candidate candidate : F1_Add_F5_AfterProperty04) {
                if (community.getDominatedCandidateIds().contains(candidate.getId())) {
                    int columnIndex = community.getId() - 1;
                    int rowIndex = candidate.getId() - 1;
                    if (disMatrix[rowIndex][columnIndex] < minDis) {
                        minDis = disMatrix[rowIndex][columnIndex];
                        minDisCandidate = candidate;
                    }
                }
            }
            //设置最短距离及其对应备选点属性
            //此处对hashmap中entry的属性进行了修改，若未重写equals和hashCode方法，那么修改前后它们的hash值就会变化，从而删不掉了。通俗点说会认为修改前后不是一个对象了
            community.setMinDis(minDis);
            community.setMinDisCandidateId(minDisCandidate.getId());
            minDisCommunityCandidate.put(community, minDisCandidate);
        }

        //获取minDisCommunityCandidate中涉及到的备选点然后去重
        System.out.println("打印各居民点及其最短距离对应的备选点：");
        for (Map.Entry<Community, Candidate> entry : minDisCommunityCandidate.entrySet()) {

            //当前服务对涉及到的两个对象
            Community curCommunity = entry.getKey();
            Candidate curCandidate = entry.getValue();

            //计算更新后的容量
            //判断F_low是否已经包含当前curCandidate，如果包含那就取出来直接更新容量即可，否则就先加入F_low中一个【崭新的】【来自于FF5的curCandidate】再更新容量
            //由于重写了equals方法，此处只要id一样就会认为是一个备选点
            if (F_up.contains(curCandidate)) {
                for (Candidate candidate : F_up) {
                    if (candidate.getId().equals(curCandidate.getId())) {
                        //注意这里是candidate
                        BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(candidate.getRemainCapacity()));
                        BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(curCommunity.getWholeNeed()));
                        double newRemainCapacity = bigDecimal_1.subtract(bigDecimal_2).doubleValue();
                        //注意这里是candidate
                        candidate.setRemainCapacity(newRemainCapacity);
                        System.out.println("curCandidate.getRemainCapacity() = " + curCandidate.getRemainCapacity());
                    }
                }
            } else {
                //注意这里是curCandidate
                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(curCandidate.getRemainCapacity()));
                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(curCommunity.getWholeNeed()));
                double newRemainCapacity = bigDecimal_1.subtract(bigDecimal_2).doubleValue();
                //注意这里是curCandidate
                curCandidate.setRemainCapacity(newRemainCapacity);
                System.out.println("curCandidate.getRemainCapacity() = " + curCandidate.getRemainCapacity());
                F_up.add(curCandidate);
            }
        }

        //优雅地对F_up进行去重，由于上面有一个F_low.contains(curCandidate)的操作，所以这里不需要去重了
//        F_up = F_up.stream().distinct().collect(Collectors.toList());
        System.out.println("=================================================");
        System.out.println("更新了容量之后的F_up：");
        F_up.forEach(System.out::println);
        System.out.println("=================================================");
        System.out.println("更新了容量之后的主函数中的F(F5)，可以看到同步发生了变化，当前这种浅拷贝是有利于我的，但换个场景就不一定了，现在已经同一改为深拷贝了：");
        MainAlgorithm.F.forEach(System.out::println);


        //5.1-若|Fup|≤K，且∀fk∊Fup，均有rk≥0，则得到最优解，best_q=best_q+，F*=Fup，整个算法结束；
        System.out.println("F_up.size() = " + F_up.size());
        if (F_up.size() <= K) {
            boolean surpassFlag = false;
            for (Candidate candidate : F_up) {
                if (candidate.getRemainCapacity() < 0) {
                    System.out.println("转至Step6调整超出的备选点数量！");
                    surpassFlag = true;
                    break;
                }
            }

            //如果没有过载点，那么就直接得到最优解，否则if自然结束进入下面一个步骤
            if (!surpassFlag) {
                //如果程序能顺利执行到这里而没有break的话，就直接得到了整个问题的最优解，上面的break似乎也不影响你后面的代码执行啊，这里得改
                //接下来计算该情况下的最优解
                for (Map.Entry<Community, Candidate> entry : minDisCommunityCandidate.entrySet()) {
                    Community curCommunity = entry.getKey();
                    Candidate curCandidate = entry.getValue();
                    int columnIndex = curCommunity.getId() - 1;
                    int rowIndex = curCandidate.getId() - 1;
                    double dij = disMatrix[rowIndex][columnIndex];
                    Double curUnsatisfiedNeed = curCommunity.getWholeNeed();
                    BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(cur_u));
                    BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(dij * curUnsatisfiedNeed));
                    cur_u = bigDecimal_1.add(bigDecimal_2).doubleValue();
                }
                //上界子算法直接得到最优解的情况
                System.out.println("=================================================");
                System.out.println("上界子算法直接得到最优解的情况：");
                //判断是否获得了更好的上界，然后return
                if (cur_u < u) {
                    u = best_q = cur_u;
                    F_best = F_up;
                    System.out.println("u = " + u);
                }
                return;
            }
        }

        //5.2-否则，初始化集合Ftemp={}，Etemp={}分别用于存储Step6中由性质4确定的必须开设的备选点及其对应的居民点，
        //然后转至Step6对超出的选址点数量进行调整；
        List<Candidate> F_temp = new ArrayList<>();
        List<Community> E_temp = new ArrayList<>();

        //6.1-通过遍历minDisCommunityCandidate，获取fj及其{ei}，互换键和值
        LinkedHashMap<Candidate, List<Community>> hashMap = new LinkedHashMap<>();
        for (Map.Entry<Community, Candidate> entry : minDisCommunityCandidate.entrySet()) {
            //如果已经存在该备选点了，那就继续添加就行了
            if (hashMap.containsKey(entry.getValue())) {
                List<Community> existCommunities = hashMap.get(entry.getValue());
                existCommunities.add(entry.getKey());
                hashMap.put(entry.getValue(), existCommunities);
                continue;
            }
            //如果该备选点不存在，那就是一个新的
            List<Community> list = new ArrayList<>();
            list.add(entry.getKey());
            hashMap.put(entry.getValue(), list);
        }

        System.out.println("=================================================");
        System.out.println("通过遍历minDisCommunityCandidate，获取fj及其{ei}，hashMap：");
        for (Map.Entry<Candidate, List<Community>> entry : hashMap.entrySet()) {
            System.out.println("entry.getKey().getId() = " + entry.getKey().getId());
            for (Community community : entry.getValue()) {
                System.out.print(community.getId() + "\t");
            }
            System.out.println();
        }

        //6.2-定义F_up\F1\F_temp=F\F0\F1\F_temp=F5(F)\F_temp，
        //现在的F本来就是F5=F\F0\F1=Fup\F1，现在的E本来就是E\E1，它们都只需要各自去掉Ftemp，Etemp即可
        List<Candidate> Fup_Minus_F1_Minus_Ftemp = new ArrayList<>(F);
        List<Community> E_Minus_E1_Minus_Etemp = new ArrayList<>(E);

        System.out.println("=================================================");
        System.out.println("开始进入while循环执行算法中的Step6");

        breakWhileFlag:
        while (true) {
            //这里的代码逻辑出现了问题，因为选出来的【Fup】对于【某些居民点】来说可能就已经是单度点了【案例中刚好没有任何居民点对于Fup为单度居民点】，所以案例的偶然性会掩盖很多问题
            //所以在循环内部，要先进行性质4的判断，然后再进行其它的事情，这样【即使后面走了另外的分支而转向了【Step8】】或者【退出上界子算法】了，
            //也能保证Fup_Minus_F1_Minus_Ftemp删掉了F_temp并且E_Minus_E1_Minus_Etemp删掉了E_temp
            //而不是先进行其它的事情，再进行性质4的判断
            //利用性质4，填充Etemp和Ftemp
            //此时：要传入性质4的为E\E1\Etemp2=E\Etemp2和已经删除掉deletedCandidate之后的Fup\{deletedCandidate}
            LinkedHashMap<Community, Candidate> singleCommunityCandidate2 = Properties.property04(F_up, E_Minus_E1_Minus_Etemp);

            if (!singleCommunityCandidate2.isEmpty()) {
                System.out.println("=======================================");
                System.out.println("调用性质4获取到的单度居民点和备选点的键值对：");
                for (Map.Entry<Community, Candidate> entry : singleCommunityCandidate2.entrySet()) {
                    System.out.println(entry);
                    F_temp.add(entry.getValue());
                    E_temp.add(entry.getKey());
                }

                //由于可能会有多个居民点指向同一个备选点，故对备选点集合F_temp进行去重
                F_temp = F_temp.stream().distinct().collect(Collectors.toList());
                //从Fup_Minus_F1_Minus_Ftemp中移除F_temp，从E_Minus_E1_Minus_Etemp中移除E_temp，更新完这些之后重新进入while循环
                Fup_Minus_F1_Minus_Ftemp.removeAll(F_temp);
                E_Minus_E1_Minus_Etemp.removeAll(E_temp);

                //用完就清掉
                F_temp.clear();
                E_temp.clear();
            }

            //|Fup|>K且|Fup\F1\Ftemp|=0，返回正无穷作为上界
            if (Fup_Minus_F1_Minus_Ftemp.size() == 0 && F_up.size() > K) {
                u = Double.MAX_VALUE;  //返回正无穷作为上界
                System.out.println("上界子算法无解，将会返回＋∞作为上界");
                return;
            }

            if (F_up.size() <= K) {
                for (Candidate candidate : F_up) {
                    if (candidate.getRemainCapacity() < 0) {
                        //∀fk∊Fup， rk≥0不成立
                        System.out.println("转至Step8继续调整超载的备选点！");
                        //这里应该把while也break掉！！！，想想办法怎么弄
                        break breakWhileFlag;
                    }
                }

                //∀fk∊Fup， rk≥0成立
                //如果程序能顺利执行到这里而没有break的话，说明只需修K而无需修hj，上界已经就已经得到了，Fbest=Fup
                //如何计算当前上界呢？留着后面再弄==>只需要将hashMap中存储的对应关系的距离*需求量加到best_q上即可
                for (Map.Entry<Candidate, List<Community>> entry : hashMap.entrySet()) {
                    //打印只需修K而无需修hj时当前的对应服务关系
                    System.out.println("打印只需修K而无需修hj时当前的对应服务关系");
                    System.out.println("entry.getKey().getId() = " + entry.getKey().getId());
                    for (Community community : entry.getValue()) {
                        System.out.print(community.getId() + "\t");
                    }
                    System.out.println();

                    Candidate curCandidate = entry.getKey();
                    List<Community> curCommunities = entry.getValue();
                    int rowIndex = curCandidate.getId() - 1;
                    for (Community curCommunity : curCommunities) {
                        int columnIndex = curCommunity.getId() - 1;
                        double curDis = MainAlgorithm.disMatrix[rowIndex][columnIndex];
                        double wholeNeed = curCommunity.getWholeNeed();  //注意此处需求还不需要分，所以可以是getWholeNeed
                        BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(cur_u));
                        BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(curDis * wholeNeed));
                        cur_u = bigDecimal_1.add(bigDecimal_2).doubleValue();
                    }
                }

                System.out.println("=================================================");
                System.out.println("只需修K而无需修hj，上界已经就已经得到了：");
                //判断是否获得了更好的上界，然后return
                if (cur_u < u) {
                    u = best_q = cur_u;
                    F_best = F_up;
                    System.out.println("u = " + u);
                }
                return;
            }

            //如果上面的三种情况都不成立，那么就进入Step6的核心调整部分了
            //此时应以上面的hashMap作为强力辅助，其key为fj，其value为【最短距离】服务的ei集合
            //由于Fup_Minus_F1_Minus_Ftemp已经去掉了Ftemp中的点，所以Etemp中的点也不可能出现在从Fup_Minus_F1_Minus_Ftemp中的
            //fk对应从hashMap中获取到的居民点集合了
            for (Candidate fk : Fup_Minus_F1_Minus_Ftemp) {
                //hashMap中包含的备选点为F\F0\F1\{上一轮删去的那个点}，始终大于等于F\F0\F1\F_temp\{上一轮删去的那个点}，
                //所以下面一定能获取到对应的当前遍历到的candidate的E_temp_fk
                List<Community> E_temp_fk = hashMap.get(fk);
                //如果有某个节点没有以最短距离服务某个居民点，那么删它是无伤的，所以setWholeLoss(0.0)
                if (E_temp_fk == null || E_temp_fk.size() == 0) {
                    fk.setWholeLoss(0.0);
                }

                if (E_temp_fk != null && E_temp_fk.size() != 0) {
                    //定义adjust_sum(fk)
                    double adjust_sum_fk = 0.0;
                    //遍历E_temp_fk中的每个居民点ei
                    for (Community ei : E_temp_fk) {
                        int rowIndex = fk.getId() - 1;
                        int columnIndex = ei.getId() - 1;
                        //当前fk<==>ei就是最短距离对，故直接从矩阵中读取最短距离
                        double minDis = MainAlgorithm.disMatrix[rowIndex][columnIndex];
                        //将当前Fup再去掉{fk}再找最短距离就是次短距离了
                        List<Candidate> Fup_Minus_fk = new ArrayList<>(F_up);
                        //去掉fk，此处使用了一种很新的东西
                        Fup_Minus_fk.removeIf(candidate -> candidate.getId().equals(fk.getId()));
                        //遍历Fup_Minus_F1_Minus_Ftemp_Minus_fk获取次短距离及其对应的fh
                        double minDis2 = Double.MAX_VALUE;
                        Candidate minDis2Candidate = null;
                        for (Candidate fh : Fup_Minus_fk) {
                            if (fh.getSlaveCommunityIds().contains(ei.getId())) {
                                int columnIndex2 = ei.getId() - 1;
                                int rowIndex2 = fh.getId() - 1;
                                if (disMatrix[rowIndex2][columnIndex2] < minDis2) {
                                    minDis2 = disMatrix[rowIndex2][columnIndex2];
                                    minDis2Candidate = fh;
                                }
                            }
                        }
                        //循环结束后就获取到了次短距离及其对应的fh，设置次短距离及其对应备选点属性
                        //此处对hashmap中entry的属性进行了修改，若未重写equals和hashCode方法，那么修改前后它们的hash值就会变化，从而删不掉了。通俗点说会认为修改前后不是一个对象了
                        ei.setMinDis2(minDis2);
                        ei.setMinDis2CandidateId(minDis2Candidate.getId());
                        //计算本轮的损失值
                        BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(adjust_sum_fk));
                        BigDecimal bigDecimal_2 = new BigDecimal(Double.toString((minDis2 - minDis) * ei.getWholeNeed()));
                        adjust_sum_fk = bigDecimal_1.add(bigDecimal_2).doubleValue();
                    }
                    fk.setWholeLoss(adjust_sum_fk);
                }
            }

            //对F_up\F1\F_temp中各备选点按照adjust_sum_fk的值升序排序，并取第一个
            Collections.sort(Fup_Minus_F1_Minus_Ftemp);
            System.out.println("=========================================");
            System.out.println("经过排序的Fup_Minus_F1_Minus_Ftemp：");
            Fup_Minus_F1_Minus_Ftemp.forEach(System.out::println);
            //获取到本轮要删除的备选点了
            Candidate deletedCandidate = Fup_Minus_F1_Minus_Ftemp.get(0);

            //从hashMap中获取其当前属下的{ei}，转接至次短距离的备选点
            List<Community> alterCommunities = new ArrayList<>();
            //遍历寻找要删除的这个备选点的最短服务距离居民点集合，其实本来不用那么麻烦的，但是不知道为什么失效了
            for (Map.Entry<Candidate, List<Community>> entry : hashMap.entrySet()) {
                if (entry.getKey().getId().equals(deletedCandidate.getId())) {
                    alterCommunities = entry.getValue();
                    break;
                }
            }

            for (Community alterCommunity : alterCommunities) {
                //获取要调整的居民点在Fup_Minus_fk中的次短距离对应的备选点的id
                Integer minDis2CandidateId = alterCommunity.getMinDis2CandidateId();
                //此处的问题是，hashMap中存储的是在找E\E1的最短距离时涉及到的备选点，它可能包含了F1中的备选点，也可能没包含，
                //而alterCommunity的次短距离的备选点却可能在F1中而hashMap中没有，从而就找不到
                //==>解决方案是：找得到就添上去，找不到就向hashMap中增加一个entry
                boolean searchFlag = false;
                //从hashMap中遍历寻找minDis2CandidateId对应的那个备选点，并将alterCommunity加入该备选点之的最短服务距离居民点集合
                for (Map.Entry<Candidate, List<Community>> entry : hashMap.entrySet()) {
                    if (entry.getKey().getId().equals(minDis2CandidateId)) {
                        searchFlag = true;
                        //获取当前键值对的value，也即居民点集合
                        List<Community> curCommunities = entry.getValue();
                        //将alterCommunity加入这个集合中
                        curCommunities.add(alterCommunity);
                        //修改剩余容量
                        BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(entry.getKey().getRemainCapacity()));
                        BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(alterCommunity.getWholeNeed()));
                        entry.getKey().setRemainCapacity(bigDecimal_1.subtract(bigDecimal_2).doubleValue());
                        //总是出莫名其妙的问题，只能先删再添了。替换
                        hashMap.replace(entry.getKey(), curCommunities);
                        break;
                    }
                }
                //如果hashMap中找不到次短距离对应的备选点，就向hashMap中增加一个entry
                if (!searchFlag) {
                    //从Fup中寻找已经调整过剩余容量的对应的备选点，此处不能从FF5中弄一个崭新的过来
                    for (Candidate candidate : F_up) {
                        if (candidate.getId().equals(minDis2CandidateId)) {
                            List<Community> list = new ArrayList<>();
                            list.add(alterCommunity);
                            hashMap.put(candidate, list);
                            break;
                        }
                    }
                }
            }

            //转接完毕后，可以从hashMap中删除以deletedCandidate为key的这个键值对了
            hashMap.keySet().removeIf(candidate -> candidate.getId().equals(deletedCandidate.getId()));
            System.out.println("==================================================");
            System.out.println("将删去的备选点所辖的各最短距离居民点转接至次短服务距离备选点之后的hashMap：");
            for (Map.Entry<Candidate, List<Community>> entry : hashMap.entrySet()) {
                System.out.println(entry);
            }

            //分别在Fup中和Fup\F1\Ftemp中删除该备选点
            F_up.removeIf(candidate -> candidate.getId().equals(deletedCandidate.getId()));
            Fup_Minus_F1_Minus_Ftemp.removeIf(candidate -> candidate.getId().equals(deletedCandidate.getId()));
        }

        System.out.println("=======================================");
        System.out.println("进入Step8之前各集合的情况：");
        System.out.println("F_up：");
        F_up.forEach(System.out::println);
        System.out.println("E：");
        E.forEach(System.out::println);
        System.out.println("E1：");
        E1.forEach(System.out::println);

        System.out.println("==========================================================================");
        //8.将Fup中所有的过载备选点加入集合Fol，然后按照如下步骤执行：
        List<Candidate> F_ol = new ArrayList<>();
        for (Candidate candidate : F_up) {
            if (candidate.getRemainCapacity() < 0.0) {
                F_ol.add(candidate);
            }
        }

        //注意：在进入Step8之前，都没有涉及到分需求的情况，使用hashMap存储服务关系是可以的，但是马上Step8中涉及到分需求了
        //所以需要将hashMap中的信息转为一个List<ServeCouple>，后续就对List<ServeCouple>进行操作了
        List<ServeCouple> serveCouples = new ArrayList<>();
        for (Map.Entry<Candidate, List<Community>> entry : hashMap.entrySet()) {
            Candidate curCandidate = entry.getKey();
            int rowIndex = curCandidate.getId() - 1;
            List<Community> curCommunities = entry.getValue();
            for (Community curCommunity : curCommunities) {
                int columnIndex = curCommunity.getId() - 1;
                serveCouples.add(new ServeCouple(curCommunity.getId(), curCandidate.getId(), curCommunity.getWholeNeed(), disMatrix[rowIndex][columnIndex]));
            }
        }
        System.out.println("=======================================");
        System.out.println("将hashMap中的信息转为一个List<ServeCouple>：");
        serveCouples.forEach(System.out::println);

        //注意：利用性质4排除单度居民点和对应的备选点，在Step6中一进去就要进行，此时是在

        //8(1)遍历Fol中各备选点fj，令Etemp (fj)= N(G[E\E1\Etemp], fj)；
        //E\E1\Etemp代表的是还能调的，还有次短距离的居民点
        for (Candidate fj : F_ol) {
            //从hashMap中获取当前备选点以【最短距离】服务的居民点集合，或者从serveCouples获取也是可以的
            List<Community> communities = hashMap.get(fj);
            //注意hashMap中get到的居民点列表可能包含刚刚上面Etemp中的居民点，所以这里要将communities和去掉了Etemp的E_Minus_E1_Minus_Etemp
            //取个交集，才能作为最终的E_temp_fj
            List<Community> E_temp_fj = E_Minus_E1_Minus_Etemp.stream().filter(communities::contains).collect(Collectors.toList());
//            for (Community community : E_Minus_E1_Minus_Etemp) {
//                if (community.getDominatedCandidateIds().contains(fj.getId())) {
//                    E_temp_fj.add(community);
//                }
//            }

            //8(2)对于Etemp(fj)中各居民点ei，计算{min2_d(G[Fup], ei)- min_d(G[Fup], ei)}值，记为adjust(ei)，
            //该值的含义为：居民点ei的单位需求量转接至Fup中其它备选点至少会增加的目标函数值；
            //将Etemp(fj)中各居民点按照adjust(ei)值从小到大排序；
            for (Community ei : E_temp_fj) {
                //并且：E\E1\Etemp代表的是还能调的，还有次短距离的居民点，所以算出来应该是一定同时有最短距离和次短距离的
                //能不能一次性计算最短距离和次短距离呢？==>这样不行，如果第一次就找到了最短距离，那么次短距离永远都不会更新

                //注意hashMap中存储有当前E\E1\Etemp中的各居民点当前的最短距离，我们只需要找到其对应的备选点，然后删掉它，再找最短距离就是次短距离了
                //现在serveCouples中也存储有当前E\E1\Etemp中的各居民点当前的最短距离，我们只需要找到其对应的备选点，然后删掉它，再找最短距离就是次短距离了
                double minDis = Double.MAX_VALUE;
                int minDisCandidateId = -1;
                for (ServeCouple serveCouple : serveCouples) {
                    if (serveCouple.getCommunityId().equals(ei.getId())) {
                        minDis = disMatrix[serveCouple.getCandidateId() - 1][serveCouple.getCommunityId() - 1];
                        minDisCandidateId = serveCouple.getCandidateId();
                    }
                }

//                for (Map.Entry<Candidate, List<Community>> entry : hashMap.entrySet()) {
//                    for (Community community : entry.getValue()) {
//                        if (community.getId().equals(ei.getId())) {
//                            minDis = disMatrix[entry.getKey().getId() - 1][community.getId() - 1];
//                            minDisCandidateId = entry.getKey().getId();
//                        }
//                    }
//                }

                //将当前Fup再去掉{fk}再找最短距离就是次短距离了
                List<Candidate> Fup_Minus_fk = new ArrayList<>(F_up);
                //去掉fk，此处使用了一种很新的东西
                int finalMinDisCandidateId = minDisCandidateId;
                Fup_Minus_fk.removeIf(candidate -> candidate.getId().equals(finalMinDisCandidateId));
                //遍历Fup_Minus_F1_Minus_Ftemp_Minus_fk获取次短距离及其对应的fh
                double minDis2 = Double.MAX_VALUE;
                int minDisCandidateId2 = -1;
                for (Candidate fh : Fup_Minus_fk) {
                    if (fh.getSlaveCommunityIds().contains(ei.getId())) {
                        int columnIndex2 = ei.getId() - 1;
                        int rowIndex2 = fh.getId() - 1;
                        if (disMatrix[rowIndex2][columnIndex2] < minDis2) {
                            minDis2 = disMatrix[rowIndex2][columnIndex2];
                            minDisCandidateId2 = fh.getId();
                        }
                    }
                }

                ei.setLossPerUnit(minDis2 - minDis);
                //打印看看计算出来的最短距离和次短距离对不对
//                System.out.println("minDis = " + minDis);
//                System.out.println("minDisCandidateId = " + minDisCandidateId);
//                System.out.println("minDis2 = " + minDis2);
//                System.out.println("minDisCandidateId2 = " + minDisCandidateId2);
            }
            //将E_temp_fj按照lossPerUnit属性从小到大排序
            Collections.sort(E_temp_fj);
            System.out.println("=======================================");
            System.out.println("将E_temp_fj按照lossPerUnit属性从小到大排序后：");
            E_temp_fj.forEach(System.out::println);

            //8(3)假设cross(fj)表示过载备选点fj的过载量；
            double cross_fj = Math.abs(fj.getRemainCapacity());
            //遍历排序后集合Etemp(fj)中的各居民点ei，
            for (Community ei : E_temp_fj) {
                //定义本轮调整量为vary，若cross(fj)>ki，则vary=ki，若cross(fj)≤ki，则vary=cross(fj)
                double vary = cross_fj > ei.getUnsatisfiedNeed() ? ei.getUnsatisfiedNeed() : cross_fj;

                //8(4)将ei在G[Fup\{fj}]中的各邻接备选点按照连接距离从小到大排序
                List<Candidate> N_Fup_Minus_fj = new ArrayList<>();

                for (Candidate candidate : F_up) {
                    //排除fj，然后判断当前是否为ei的邻接备选点
                    if (!candidate.getId().equals(fj.getId()) && candidate.getSlaveCommunityIds().contains(ei.getId())) {
                        N_Fup_Minus_fj.add(candidate);
                    }
                }

                //循环结束后得到未经排序的ei在Fup\{fj}中的邻接备选点
                System.out.println("=======================================");
                System.out.println("未经排序的ei在Fup\\{fj}中的邻接备选点：");
                for (Candidate candidate : N_Fup_Minus_fj) {
                    System.out.println(candidate);
                }
                //现在针对该列表按照到ei的距离进行排序
                int columnIndex = ei.getId() - 1;

                //使用简单的冒泡排序对该列表中的各备选点按照到ei的距离进行排序
                for (int i = 0; i < N_Fup_Minus_fj.size() - 1; i++) {
                    for (int j = 0; j < N_Fup_Minus_fj.size() - 1 - i; j++) {
                        int rowIndex = N_Fup_Minus_fj.get(j).getId() - 1;
                        int rowIndex2 = N_Fup_Minus_fj.get(j + 1).getId() - 1;
                        if (disMatrix[rowIndex][columnIndex] > disMatrix[rowIndex2][columnIndex]) {
                            Candidate temp = N_Fup_Minus_fj.get(j);
                            N_Fup_Minus_fj.set(j, N_Fup_Minus_fj.get(j + 1));
                            N_Fup_Minus_fj.set(j + 1, temp);
                        }
                    }
                }

                //经过排序的ei在Fup\{fj}中的邻接备选点
                System.out.println("=======================================");
                System.out.println("经过排序的ei在Fup\\{fj}中的邻接备选点：");
                for (Candidate candidate : N_Fup_Minus_fj) {
                    System.out.println(candidate);
                }

                //遍历N(G[Fup\{fj}], ei)中的各备选点fk，定义vary_2=vary
                double vary_2 = vary;

                for (int i = 0; i < N_Fup_Minus_fj.size(); i++) {
                    Candidate fk = N_Fup_Minus_fj.get(i);

                    //若rk≥vary_2，则当前备选点fk可以【完全容纳】vary_2，将ei与fk连线，执行vary_2=0并退出循环；
                    if (fk.getRemainCapacity() >= vary_2) {
                        //记录ei与fk的绑定关系
                        //先把fj负责ei的部分需求量减掉，减去vary_2
                        for (ServeCouple serveCouple : serveCouples) {
                            if (serveCouple.getCommunityId().equals(ei.getId())) {
                                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(serveCouple.getResponsibleNeed()));
                                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(vary_2));
                                serveCouple.setResponsibleNeed(bigDecimal_1.subtract(bigDecimal_2).doubleValue());
                            }
                        }
                        //然后新增一条ei与fk的服务关系
                        serveCouples.add(new ServeCouple(ei.getId(), fk.getId(), vary_2, disMatrix[fk.getId() - 1][ei.getId() - 1]));

                        //更新fk的容量，-vary_2
                        BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(fk.getRemainCapacity()));
                        BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(vary_2));
                        fk.setRemainCapacity(bigDecimal_1.subtract(bigDecimal_2).doubleValue());
                        //更新fj的容量，+vary_2
                        BigDecimal bigDecimal_3 = new BigDecimal(Double.toString(fj.getRemainCapacity()));
                        BigDecimal bigDecimal_4 = new BigDecimal(Double.toString(vary_2));
                        fj.setRemainCapacity(bigDecimal_3.add(bigDecimal_4).doubleValue());

                        //执行vary_2=0
                        vary_2 = 0.0;

                        break;
                    }

                    //若0<rk<vary_2，则当前备选点fk可以容纳vary_2的一部分，将ei与fk连线，执行vary_2= vary_2-rk
                    if (fk.getRemainCapacity() < vary_2 && fk.getRemainCapacity() > 0.0) {
                        //记录ei与fk的绑定关系
                        //先把fj负责ei的部分需求量减掉，减去rk
                        for (ServeCouple serveCouple : serveCouples) {
                            if (serveCouple.getCommunityId().equals(ei.getId())) {
                                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(serveCouple.getResponsibleNeed()));
                                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(fk.getRemainCapacity()));
                                serveCouple.setResponsibleNeed(bigDecimal_1.subtract(bigDecimal_2).doubleValue());
                            }
                        }
                        //然后新增一条ei与fk的服务关系
                        serveCouples.add(new ServeCouple(ei.getId(), fk.getId(), fk.getRemainCapacity(), disMatrix[fk.getId() - 1][ei.getId() - 1]));

                        //更新fk的容量为0
                        fk.setRemainCapacity(0.0);
                        //更新fj的容量，+rk
                        BigDecimal bigDecimal_3 = new BigDecimal(Double.toString(fj.getRemainCapacity()));
                        BigDecimal bigDecimal_4 = new BigDecimal(Double.toString(fk.getRemainCapacity()));
                        fj.setRemainCapacity(bigDecimal_3.add(bigDecimal_4).doubleValue());

                        //执行vary_2= vary_2-rk
                        BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(vary_2));
                        BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(fk.getRemainCapacity()));
                        vary_2 = bigDecimal_1.subtract(bigDecimal_2).doubleValue();
//                        continue;  //continue不必要，因为是循环中的最后一条语句
                    }

                    //若rk≤0，则当前备选点无法容纳vary_2，继续遍历N(G[Fup\{fj}], ei)中下一个元素，由于身处循环中的最后一行，所以这个逻辑不必写了
//                    if (fk.getRemainCapacity() <= 0) {
//                        continue;
//                    }
                }

                //循环有两种结束方式：①通过第一个if里面的break结束，此时vary_2=0，本轮修复成功；②自然地遍历结束，此时vary_2>0，本轮修复没有成功。
                //所以此处判断循环结束后，若仍有vary_2>0，说明该备选点的【部分超载需求】vary无法得到修复，此时上界子算法无解，返回+∞作为上界，上界子算法结束
                if (vary_2 > 0.0) {
                    System.out.println("上界子算法无解，将会返回＋∞作为上界");
                    u = u_limit;
                    return;
                }

                //第(4)步中循环结束后如果vary_2 = 0.0，则执行cross(fj)= cross(fj)-vary
                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(cross_fj));
                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(vary));
                cross_fj = bigDecimal_1.subtract(bigDecimal_2).doubleValue();

                //若cross(fj)=0，则当前过载点fj已修复完毕，退出第(3)步中循环，继续修复下一个过载点
                //若cross(fj)>0，则仅修复了当前过载点的部分需求，继续进行循环，来修复余下的需求
                if (cross_fj == 0) {
                    break;
                }
            }
        }

        //如果能顺利走到这里，而没有在修复时进入上界子算法无解的if，就代表修复成功了
        System.out.println("=======================================");
        System.out.println("修复完毕后的ServeCouple：");
        serveCouples.forEach(System.out::println);

        //根据serveCouples计算上界，并且Fbest=Fup
        for (ServeCouple serveCouple : serveCouples) {
            BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(cur_u));
            BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(serveCouple.getResponsibleNeed() * serveCouple.getDistance()));
            cur_u = bigDecimal_1.add(bigDecimal_2).doubleValue();
        }

        System.out.println("=======================================");
        System.out.println("修复完毕后的F_up：");
        F_up.forEach(System.out::println);
        //赋值操作
        if (cur_u < u) {
            u = best_q = cur_u;
            F_best = F_up;
        }
        System.out.println("=======================================");
        System.out.println("全局变量：u = " + u);
        System.out.println("保存降阶中固定下来的的目标值：fixedValue = " + fixedValue);

//        System.out.println("=======================================F");
//        F.forEach(System.out::println);
//        System.out.println("=======================================F1");
//        F1.forEach(System.out::println);
//        System.out.println("=======================================E");
//        E.forEach(System.out::println);
    }

    private static CloneObject cloneObject(List<Candidate> F, List<Candidate> F0, List<Candidate> F1, List<Community> E, List<Community> E1) {
        List<Candidate> F_u = new ArrayList<>();
        for (Candidate candidate : F) {
            F_u.add(candidate.clone());
        }
        List<Candidate> F0_u = new ArrayList<>();
        for (Candidate candidate : F0) {
            F0_u.add(candidate.clone());
        }
        List<Candidate> F1_u = new ArrayList<>();
        for (Candidate candidate : F1) {
            F1_u.add(candidate.clone());
        }

        List<Community> E_u = new ArrayList<>();
        for (Community community : E) {
            E_u.add(community.clone());
        }
        List<Community> E1_u = new ArrayList<>();
        for (Community community : E1) {
            E1_u.add(community.clone());
        }

        return new CloneObject(F_u, F0_u, F1_u, E_u, E1_u);
    }
}
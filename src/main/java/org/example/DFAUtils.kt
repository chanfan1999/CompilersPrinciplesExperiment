package org.example

import org.example.NFAUtils.Companion.EPSILON
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

fun main() {
    val r = NFAUtils.regexToNFA("a|bc")
    val status = NFAUtils.countStatusNumber("a|bc")
    DFAUtils.toDFA(r, status)
}

class DFAUtils {
    companion object {
        fun toDFA(nfaResult: ArrayList<Node>, status: ArrayList<String>) {
            val epsilonList = esList(nfaResult)//储存每个节点的epsilon闭包
            val resultMap = HashMap<HashSet<Int>,Int>()
            val statusQueue = ArrayDeque<HashSet<Int>>() as Queue<HashSet<Int>>
            var num = 0
            resultMap[epsilonList[0]] = num++
            statusQueue.add(HashSet(epsilonList[0]))
            while (statusQueue.isNotEmpty()){
                val originStatus = statusQueue.poll()
                status.forEach { r->
                    val tempStatus = HashSet<Int>()
                    originStatus.forEach { i->
                        nfaResult[i].nextList.filter {pair: Pair<Node, String> ->
                            pair.second == r
                        } .forEach {
                            tempStatus.addAll(epsilonList[it.first.num])
                        }
                    }
                    if (!resultMap.containsKey(tempStatus)&&tempStatus.isNotEmpty()){
                        resultMap[tempStatus] = num++
                        //todo 标记tempStatus为新状态，将得到的过程储存起来，如A+a
                        statusQueue.add(tempStatus)
                    }
                }
            }
            println(resultMap)


        }

        fun esList(nfaResult: ArrayList<Node>): ArrayList<HashSet<Int>> {
            val statusList = ArrayList<HashSet<Int>>()
            for (i in nfaResult) {
                val hs = HashSet<Int>()
                hs.add(i.num)
                val queue = ArrayDeque<Int>() as Queue<Int>
                val numberExist = HashMap<Int, Int>()
                queue.add(i.num)
                while (queue.isNotEmpty()) {
                    val t = queue.poll()
                    nfaResult[t].nextList.filter { it.second == EPSILON }.forEach { node ->
                        if (!numberExist.containsKey(node.first.num)) {
                            queue.add(node.first.num)
                            hs.add(node.first.num)
                            numberExist.put(node.first.num, 1)
                        }
                    }
                }
                statusList.add(hs)
            }
            println(statusList)
            return statusList
        }
    }

}
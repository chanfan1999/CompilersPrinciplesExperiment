package org.example

import org.example.NFAUtils.Companion.EPSILON
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

fun main() {
    val r = NFAUtils.regexToNFA("a|bc")
    val status = NFAUtils.countStatusNumber("a|bc")
    val d = DFAUtils.toDFA(r, status)
    println("1")
}

class DFAUtils {
    companion object {
        val nameMap = HashMap<DFANode, String>()
        fun toDFA(nfaResult: ArrayList<Node>, status: ArrayList<String>): ArrayList<DFANode> {
            val epsilonList = esList(nfaResult)//储存每个节点的epsilon闭包
            val statusExists = HashMap<DFANode, Int>()
            val statusQueue = ArrayDeque<HashSet<Int>>() as Queue<HashSet<Int>>
            val dfaResult = ArrayList<DFANode>()
            statusExists[DFANode(epsilonList[0])] = 1
            statusQueue.add(HashSet(epsilonList[0]))
            while (statusQueue.isNotEmpty()) {
                val originStatus = statusQueue.poll()
                val originNode = DFANode(originStatus)
                status.forEach { r ->
                    val tempStatus = HashSet<Int>()
                    originStatus.forEach { i ->
                        nfaResult[i].nextList.filter { pair: Pair<Node, String> ->
                            pair.second == r
                        }.forEach {
                            tempStatus.addAll(epsilonList[it.first.num])
                        }
                    }
                    if (tempStatus.isEmpty()) {
                        //空集情况
                        originNode.toStatus[r] = null
                    } else {
                        val tempNode = DFANode(tempStatus)
                        if (statusExists.containsKey(tempNode)) {
                            //指向已有状态
                            originNode.toStatus[r] = tempNode
                        } else {
                            //增加新状态
                            originNode.toStatus[r] = tempNode
                            statusExists[tempNode] = 1
                            statusQueue.add(tempStatus)
                        }
                    }
                }
                dfaResult.add(originNode)
            }
            nameNode(dfaResult)
            return dfaResult
        }

        private fun esList(nfaResult: ArrayList<Node>): ArrayList<HashSet<Int>> {
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

        private fun nameNode(dfaResult: ArrayList<DFANode>) {
            var name = 'A'
            for (i in dfaResult) {
                nameMap[i] = name.toString()
                name += 1
            }
        }

        //todo 完成dfa文本展示
        fun toDFAText(dfaResult: ArrayList<DFANode>): String {
            val sb = StringBuilder()
            for (i in dfaResult) {
                sb.append("节点${i.name}:\n")
                for ((k, v) in i.toStatus) {
                    sb.append(k)
                    sb.append("-->")
                    if (v == null) {
                        sb.append("空集  ")
                    } else {

                    }
                }
            }
            return sb.toString()
        }
    }
}
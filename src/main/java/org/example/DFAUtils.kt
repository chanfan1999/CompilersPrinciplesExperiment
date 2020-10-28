package org.example

import org.example.NFAUtils.Companion.EPSILON
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


class DFAUtils {
    companion object {
        private val nameMap = HashMap<DFANode, String>()

        fun getMinDFAText(minList: ArrayList<MinDFANode>): String {
            val sb = StringBuilder()
            sb.append("最小化DFA结果包含${minList.size}种状态：\n")
            for (i in minList) {
                sb.append("状态${i.name}：\n")
                for ((k,v) in i.toStatus){
                    sb.append("$k-->$v  ")
                }
                sb.append('\n')
            }
            return sb.toString()
        }

        fun toDFA(nfaResult: ArrayList<NFANode>, status: ArrayList<String>, endNFANodeNum: Int): ArrayList<DFANode> {
            nameMap.clear()
            val epsilonList = getEpsilonList(nfaResult)//储存每个节点的epsilon闭包
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
                        nfaResult[i].nextList.filter { pair: Pair<NFANode, String> ->
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
            markEndNode(dfaResult, endNFANodeNum)
            return dfaResult
        }

        private fun getEpsilonList(nfaResult: ArrayList<NFANode>): ArrayList<HashSet<Int>> {
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

        private fun markEndNode(dfaResult: ArrayList<DFANode>, nfaEndNodeNumber: Int) {
            dfaResult.forEach {
                if (it.containList.contains(nfaEndNodeNumber)) {
                    it.isEndStatus = true
                }
                if (it.containList.contains(0)){
                    it.isBeginStatus = true
                }
            }
        }

        fun getDFATableText(dfaResult: ArrayList<DFANode>): String {
            nameNode(dfaResult)
            val sb = StringBuilder()
            for (i in dfaResult) {
                sb.append("状态${nameMap[i]}包括:\n")
                for (j in i.containList) {
                    sb.append(j)
                    sb.append(" ")
                }
                sb.append('\n')
            }

            for (i in dfaResult) {
                sb.append("状态${nameMap[i]}:\n")
                for ((k, v) in i.toStatus) {
                    sb.append(k)
                    sb.append("-->")
                    if (v == null) {
                        sb.append("空集  ")
                    } else {
                        sb.append(nameMap[v] + "  ")
                    }
                    sb.append('\n')
                }
            }
            return sb.toString()
        }

        fun minimizeDFA(dfaResult: ArrayList<DFANode>, status: ArrayList<String>): ArrayList<MinDFANode> {
            val endSet = HashSet<DFANode>()
            val nonEndSet = HashSet<DFANode>()
            endSet.addAll(dfaResult.filter { node ->
                node.isEndStatus
            })
            nonEndSet.addAll(dfaResult.filter { node ->
                !node.isEndStatus
            })
            val targetList = ArrayList<HashSet<DFANode>>()
            targetList.add(nonEndSet)
            targetList.add(endSet)
            val stack = Stack<HashSet<DFANode>>()
            stack.push(nonEndSet)
            stack.push(endSet)
            while (stack.isNotEmpty()) {
                val set = stack.pop()
                if (set.isNotEmpty() && set.size > 1) {
                    for (i in status) {
                        val temp = set.stream().filter { n -> n.toStatus[i] != null }.collect(Collectors.groupingBy { node ->
                            node.toStatus[i]?.let { inWhichSet(it, targetList) }
                        })
                        val newSet = HashSet<DFANode>()
                        for ((_, v) in temp) {
                            v?.let { newSet.addAll(it) }
                        }
                        if (newSet.size != set.size && newSet.isNotEmpty()) {
                            targetList.remove(set)
                            targetList.add(newSet)
                            set.removeAll(newSet)
                            targetList.add(set)
                            stack.push(newSet)
                            stack.push(set)
                            break
                        }
                    }
                }
            }
            return nameMinNode(targetList, status)
        }

        private fun nameMinNode(targetList: ArrayList<HashSet<DFANode>>, status: ArrayList<String>): ArrayList<MinDFANode> {
            var name = 'A'
            val minNodeList = ArrayList<MinDFANode>()
            for (i in targetList) {
                val tempNode = MinDFANode()
                tempNode.name = name.toString()
                tempNode.set.addAll(i)
                tempNode.standNode = i.first()
                minNodeList.add(tempNode)
                name += 1
            }

            for (i in minNodeList) {
                for (s in status) {
                    val t = i.standNode.toStatus[s]
                    for (j in minNodeList) {
                        if (j.set.contains(t)) {
                            i.toStatus[s] = j.name
                            break
                        }
                    }
                }
            }
            return minNodeList
        }

        private fun inWhichSet(n: DFANode, list: ArrayList<HashSet<DFANode>>): Int {
            for ((index, i) in list.withIndex()) {
                if (i.contains(n))
                    return index
            }
            return 0
        }
    }


}

package org.example

import org.example.NFAUtils.Companion.EPSILON
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

fun main() {
    val r = NFAUtils.regexToNFA("a|bc")
    DFAUtils.esList(r)
}

class DFAUtils {
    companion object {
        fun toDFA(nfaResult: ArrayList<Node>) {

        }

        fun esList(nfaResult: ArrayList<Node>):ArrayList<HashSet<Int>> {
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
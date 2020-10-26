package org.example

import org.example.NFAUtils.Companion.EPSILON

fun main() {
    val r = NFAUtils.regexToNFA("a|bc")
    DFAUtils.esList(r)
}

class DFAUtils {
    companion object {
        fun toDFA(nfaResult: ArrayList<Node>) {

        }

        fun esList(nfaResult: ArrayList<Node>) {
            val statusList = ArrayList<HashSet<Int>>()
            for ((index, i) in nfaResult.withIndex()) {
                val hs = HashSet<Int>()
                hs.add(index)
                val ls = ArrayList<Int>()
                i.nextList.filter { it.second == EPSILON }.forEach { node ->
                    ls.add(node.first.num)
                }
                for (j in ls) {
                    for (k in nfaResult[j].nextList) {
                        if (k.second == EPSILON) {
                            ls.add(k.first.num)
                        }
                    }
                }
                hs.addAll(ls)
                statusList.add(hs)
            }
            println(statusList)
        }
    }

}
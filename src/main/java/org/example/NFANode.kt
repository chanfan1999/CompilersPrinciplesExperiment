package org.example

class NFANode {
    val nextList: ArrayList<Pair<NFANode, String>> = ArrayList()
    var num = 0
}
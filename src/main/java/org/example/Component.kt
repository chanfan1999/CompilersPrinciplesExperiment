package org.example

class Component(val headNode: NFANode = NFANode(), val tailNode: NFANode = NFANode(), r: String = "") {
    init {
        if (r.isNotEmpty()) {
            headNode.nextList.add(Pair(tailNode, r))
        }
    }
}
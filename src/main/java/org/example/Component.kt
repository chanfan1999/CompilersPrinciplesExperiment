package org.example

class Component(val headNode: Node = Node(), val tailNode: Node = Node(), r: String = "") {
    init {
        if (r.isNotEmpty()) {
            headNode.nextList.add(Pair(tailNode, r))
        }
    }
}
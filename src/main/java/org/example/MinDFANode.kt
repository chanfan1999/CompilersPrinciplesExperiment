package org.example

class MinDFANode {
    val set = HashSet<DFANode>()
    var name = ""
    val toStatus = HashMap<String,String>()
    lateinit var standNode:DFANode
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MinDFANode

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}
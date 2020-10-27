package org.example
class DFANode(hashSet: HashSet<Int>) {
    val containList: HashSet<Int> = hashSet
    val toStatus = HashMap<String, DFANode?>()
    lateinit var name: String
    var isEndStatus: Boolean = false
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DFANode) return false

        if (containList != other.containList) return false

        return true
    }

    override fun hashCode(): Int {
        return containList.hashCode()
    }

}
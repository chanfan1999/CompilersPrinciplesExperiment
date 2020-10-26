package org.example

class DFANode {
    val containList = HashSet<Int>()
    lateinit var name: String
    val from = ArrayList<DFANode>()
}
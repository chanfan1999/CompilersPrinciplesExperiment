package org.example


import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class DFANode(val ruleSet: HashMap<String, ArrayList<String>>,val nodeSet: HashSet<DFANode>) {

    var number = 0
    val itemArrayList = ArrayList<Item>()
    val nextMap = HashMap<String, DFANode>()

    companion object {
        fun from(consumeStr: String, originNode: DFANode, position: Int): DFANode {
            // 构造要返回的节点
            val node = DFANode(originNode.ruleSet,originNode.nodeSet)
            // 消耗字符，同时位置加一
            val tempList = ArrayList<Item>()
            originNode.itemArrayList.filter {
                it.content.length>it.position&&it.content[it.position].toString() == consumeStr
            }.onEach {
                tempList.add(Item(it.symbol,it.content,it.position+1))
            }
            node.itemArrayList.addAll(tempList)

            // 消耗字符后

            val stack = Stack<Item>()
            node.itemArrayList.forEach {
                stack.push(it)
            }
            while (stack.isNotEmpty()) {
                val i = stack.pop()
                if (i.position < i.content.length) {
                    if (i.content[i.position].isUpperCase()) {
                        node.addItems(i.content[i.position].toString()).forEach {
                            if (!node.itemArrayList.contains(it)){
                                stack.push(it)
                                node.itemArrayList.add(it)
                            }
                        }
                    }
                }
            }

            if (node.nodeSet.contains(node)){
                val t =  node.nodeSet.find { it==node }
                t?.let {
                    return t
                }
                // 不会执行这个
                return node
            }else{
                node.nodeSet.add(node)
                node.getNextNodes(position)
                return node
            }
        }
    }



    /**
     * @param symbol 箭头左边的符号
     * @param content 代表符号指向的单条规则
     * @param position 当前规则所在位置
     *
     */




    //寻找给定符号symbol的对应语法规则集合并返回
    fun addItems(symbol: String): ArrayList<Item> {
        val tempList = ArrayList<Item>()
        ruleSet[symbol]?.forEach {
            tempList.add(Item(symbol, it, 0))
            if (it[0].toString() != symbol) {
                if (it[0].isUpperCase()) {
                    tempList.addAll(addItems(it[0].toString()))
                }
            }
        }
        return tempList
    }

    fun getNextNodes(position: Int) {
        // 对指针所在位置的字母做一个去重处理,方便后续遍历获取相邻节点
        val toTravelSet = HashSet<String>()
        itemArrayList.forEach {
            if (it.position < it.content.length&&it.content[it.position]!='$') {
                toTravelSet.add(it.content[it.position].toString())
            }
        }
        itemArrayList.apply {
           val t = distinctBy { item ->
               item.hashCode()
           }
            clear()
            addAll(t)
        }
        for (i in toTravelSet) {
             val node = from(i, this, position)
            if (node==this)
                nextMap[i] = this
            else
                nextMap[i] = node
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DFANode

        for ((index,i) in this.itemArrayList.withIndex()){
            if (i != other.itemArrayList[index])
                return false
        }

        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }


}
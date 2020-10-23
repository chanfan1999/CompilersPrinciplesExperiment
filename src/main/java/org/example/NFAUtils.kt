package org.example

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

fun main(){

}

class NFAUtils{

    companion object{
        const val EPSILON = "epsilon"
        fun getNFATableText(result:ArrayList<Node>,status:ArrayList<String>):ArrayList<String>{
            status.add(EPSILON)
            val textList = ArrayList<String>()
            result.forEach { it ->
                val sb = StringBuilder()
                sb.append("节点${it.num}:\n")
                for (i in status){
                    sb.append("$i->")
                    val listTemp = it.nextList.filter {l->
                        l.second == i.toString()
                    }
                    if (listTemp.isEmpty()){
                        sb.append("空  ")
                    }else{
                        listTemp.forEach{
                            sb.append("${it.first.num}  ")
                        }
                    }
                }
                textList.add(sb.toString())
            }
            return textList
        }

        fun countStatusNumber(exp: String):ArrayList<String>{
            val m = HashSet<String>()
            val list = ArrayList<String>()
            for (i in exp){
                if (i.isLetter()&&!m.contains(i.toString())){
                    list.add(i.toString())
                    m.plus(i.toString())
                }
            }
            return list
        }

        fun regexToNFA(exp: String):ArrayList<Node>{
            return numberNode(toPostFix(exp))
        }

        fun toPostFix(exp: String): Component {
            var lastIsLetter = false
            val sb = StringBuilder()
            for (i in exp) {
                if ((lastIsLetter && i.isLetter()) || (lastIsLetter && i == '('))
                    sb.append("+")
                if (i.isLetter()) {
                    sb.append(i)
                    lastIsLetter = true
                } else {
                    sb.append(i)
                    lastIsLetter = false
                }
            }
            println(sb.toString())
            lastIsLetter = false
            val expression = sb.toString()
            val stack = Stack<Char>()
            val queue = ArrayList<Char>()
            for (i in expression) {
                if (i.isLetter()) {
                    queue.add(i)
                    lastIsLetter = true
                } else {
                    when (i) {
                        '|', '(' -> {
                            stack.push(i)
                        }
                        '*' ->{
                            if (lastIsLetter){
                                queue.add(i)
                            }else{
                                stack.push(i)
                            }
                        }
                        ')' -> {
                            while (stack.peek() != '(') {
                                queue.add(stack.pop())
                            }
                            stack.pop()
                        }
                        '+' -> {
                            while (stack.isNotEmpty() && (stack.peek() == '*' || stack.peek() == '|')) {
                                queue.add(stack.pop())
                            }
                            stack.push(i)
                        }
                    }
                }
            }
            while (stack.isNotEmpty()) {
                queue.add(stack.pop())
            }
            println(queue)
            val result = Stack<Component>()
            for (i in queue) {
                if (!i.isLetter()) {
                    val temp = when (i) {
                        '+' -> {
                            val d2 = result.pop()
                            val d1 = result.pop()
                            and(d1, d2)
                        }
                        '*' -> {
                            val d1 = result.pop()
                            closure(d1)
                        }
                        '|' -> {
                            val d2 = result.pop()
                            val d1 = result.pop()
                            or(d1, d2)
                        }
                        else -> null
                    }
                    result.push(temp)
                } else {
                    result.push(Component(r = i.toString()))
                }
            }
            return result.peek()
        }


        private fun or(c1: Component, c2: Component): Component {
            val tempStart = Node()
            val tempEnd = Node()
            tempStart.nextList.add(Pair(c1.headNode, EPSILON))
            tempStart.nextList.add(Pair(c2.headNode, EPSILON))
            c1.tailNode.nextList.add(Pair(tempEnd, EPSILON))
            c2.tailNode.nextList.add(Pair(tempEnd, EPSILON))
            return Component(tempStart, tempEnd)
        }

        private fun and(c1: Component, c2: Component): Component {
            c1.tailNode.nextList.add(Pair(c2.headNode, EPSILON))
            return Component(c1.headNode, c2.tailNode)
        }

        private fun closure(c1: Component): Component {
            c1.tailNode.nextList.add(c1.headNode to EPSILON)//连接到开头
            val tempStart = Node()
            val tempEnd = Node()
            tempStart.nextList.add(c1.headNode to EPSILON)
            tempStart.nextList.add(tempEnd to EPSILON)
            c1.tailNode.nextList.add(tempEnd to EPSILON)
            return Component(tempStart, tempEnd)
        }

        fun numberNode(c: Component): ArrayList<Node> {
            val nodeList = ArrayList<Node>()
            val visited = HashMap<Node, Boolean>()
            val queue = ArrayDeque<Node>() as Queue<Node>
            queue.add(c.headNode)
            var i = 0
            while (queue.isNotEmpty()) {
                val n = queue.poll()
                if (!visited.containsKey(n)) {
                    n.num = i
                    i++
                    nodeList.add(n)
                    visited[n] = true
                    for (j in n.nextList) {
                        queue.add(j.first)
                    }
                }
            }
            return nodeList
        }
    }
}


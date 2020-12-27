package org.example

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

fun tell(message: String) {
    val alert = Alert(Alert.AlertType.INFORMATION)
    alert.alertTypeProperty().set(Alert.AlertType.INFORMATION)
    alert.headerText = message
    alert.show()
}

class MainController {
    private var file: File? = null

    @FXML
    private lateinit var rootLayout: VBox

    @FXML
    private lateinit var editText: TextArea

    @FXML
    private lateinit var imageView: ImageView

    @FXML
    private lateinit var firstSetArea: TextArea

    @FXML
    private lateinit var isSLR: Label

    @FXML
    private lateinit var needExpand: Label

    @FXML
    private lateinit var reason: Label

    @FXML
    private lateinit var tableArea: TextArea

    private val rawText1 = "G[S]:\n" +
                    "S->I|o\n" +
                    "I->iS|iSeS"

    private val rawText2 = "G[T]:\n" +
            "T->SB|Sc\n" +
            "S->s\n" +
            "B->b"

    private var rawText3 = "G[A]:\n" +
            "A->aAd|aAb|$"

    private val rawText4 = "G[E]:\n" +
            "E->E+n|n"

    private val rawText5 = "G[S]:\n" +
            "S->(S)S|$"

    // 有冲突例子
    private val rawText6 = "G[S]:\n" +
            "S->IETSP|O\n" +
            "I->i\n" +
            "E->b\n" +
            "O->o\n" +
            "L->e\n" +
            "T->t\n" +
            "P->LS|$"

    private val exampleList = arrayListOf(rawText1, rawText2, rawText3, rawText4, rawText5, rawText6)


    @FXML
    fun openFile() {
        file = FileChooser().showOpenDialog(rootLayout.scene.window as Stage)
        file?.apply {
            val fileReader = FileReader(this)
            editText.text = fileReader.readText()
            fileReader.close()
        }
    }

    @FXML
    fun setExample(action: ActionEvent) {
        val button = action.target as Button
        val n = button.text.last().toString().toInt()
        editText.text = exampleList[n-1]
        println()
    }


    @FXML
    fun saveFile() {
        if (file == null) {
            val fileChooser = FileChooser()
            val extFilter = FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt")
            fileChooser.extensionFilters.add(extFilter)
            val s = Stage()
            val file = fileChooser.showSaveDialog(s)
            if (file != null) {
                val fileWriter = FileWriter(file)
                fileWriter.write(editText.text)
                fileWriter.close()
            }
        } else {
            val text = editText.text
            val fileWriter = FileWriter(file)
            fileWriter.write(text)
            fileWriter.close()
        }
        tell("保存成功")
    }

    @FXML
    fun analyse() {
        makeClean()
        initData()
        getFirstSetList()
        getFollowSet()
        val node = analyseForGraph()
        makeGraphFile(node)
        val image = Image(File("temp.png").toURI().toString())
        imageView.fitHeight = image.height / 1.5
        imageView.fitWidth = image.width
        imageView.image = image
        if (!isConflicting(node)) {
            makeTable(node)
        }
    }


    // 初始化，清理集合内容
    private fun makeClean() {
        imageView.image = Image(File("welcome.png").toURI().toString())
        firstSetArea.clear()
        tableArea.clear()
        firstSet.clear()
        reason.text = ""
        ruleMap.clear()
        followSet.clear()
        symbolSet.clear()
    }


    private val symbolSet = HashSet<String>()
    private fun initData() {
        val rawText = editText.text.trim()
//        val rawText = "G[S]:\n" +
//                "S->IETSP|O\n" +
//                "I->i\n" +
//                "E->b\n" +
//                "O->o\n" +
//                "L->e\n" +
//                "T->t\n" +
//                "P->LS|$"

//        val rawText = "G[T]:\n" +
//                "T->SB|Sc\n" +
//                "S->s\n" +
//                "B->b"

//        var rawText = "G[A]:\n" +
//                "A->aAd|aAb|$"

//        val rawText = "G[E]:\n" +
//                "E->E+n|n"

//        val rawText = "G[S]:\n" +
//                "S->(S)S|$"

        // 有冲突例子
//        val rawText = "G[S]:\n" +
//                "S->I|o\n"+
//                "I->iS|iSeS"
        beginSymbol = rawText[2].toString()
        val rawList = rawText.trim().substringAfter(":\n")
        val list = ArrayList(rawList.split("\n"))
        for (i in list) {
            // 开始符号，非终结符
            val nonTerminator = i.split("->")[0]

            symbolSet.add(nonTerminator)

            // 箭头右边内容
            val content = i.split("->")[1]
            content.forEach {
                symbolSet.add(it.toString())
            }

            // 若右边内容有分隔符，则需要扩充
            if (nonTerminator == beginSymbol) {
                if (content.contains("|")) {
                    println("需要扩充")
                    needExpand.text = "是"
                    ruleMap["Z"] = arrayListOf(beginSymbol)
                    beginSymbol = "Z"
                } else {
                    needExpand.text = "否"
                }
            }
            val l = content.split("|")
            if (ruleMap.containsKey(nonTerminator)) {
                ruleMap[nonTerminator]?.addAll(l)
            } else {
                val tempList = ArrayList<String>()
                tempList.addAll(l)
                ruleMap[nonTerminator] = tempList
            }
        }
    }

    // 生成图文件和图片
    private fun makeGraphFile(node: DFANode) {
        val f = File("temp.dot")
        val bw = BufferedWriter(FileWriter(f))
        val sb = StringBuilder()
        sb.append("digraph g {\n")
        sb.append("node [shape=\"box\" , charset = \"UTF-8\" fontname = \"Microsoft Yahei\"]\n")
        val stack = Stack<DFANode>()
        stack.push(node)
        for (t in node.nodeSet) {
            t.nextMap.values.forEach {
                if (it != t)
                    stack.push(it)
            }
            sb.append(t.number)
            sb.append(" [label=\"")
            sb.append("Node ${t.number}:\n")
            for (i in t.itemArrayList) {
                if (i.position == 0) {
                    sb.append(i.symbol + "->." + i.content + "\n")
                } else {
                    sb.append(i.symbol + "->")
                    sb.append(i.content.substring(0, i.position) + ".")
                    sb.append(i.content.substring(i.position) + "\n")
                }
            }
            sb.append("\"]\n")
            for (item in t.nextMap) {
                sb.append(t.number.toString() + "->" + item.value.number)
                sb.append(" [label=\"" + item.key + "\"]\n")
            }
        }
        sb.append("}")
        bw.write(sb.toString())
        bw.flush()
        bw.close()
        println("输出完毕，路径是${f.absolutePath}")
        val commandStr = "mapbin/dot.exe -Tpng -o temp.png temp.dot"
        val p = Runtime.getRuntime().exec(commandStr)
        p.waitFor()
        p.destroy()
    }

    /*
              G[S]:
                S->IETSP|O
                I->i
                E->b
                O->o
                L->e
                T->t
                P->LS|$
*/

    // 编号节点
    private fun numberNode(node: DFANode) {
        var number = 1
//        val stack = Stack<DFANode>()
//        stack.push(node)
//        while (stack.isNotEmpty()) {
//            val t = stack.pop()
//            t.number = number++
//            t.nextMap.values.forEach {
//                if (!t.nodeSet.contains(it))
//                    stack.push(it)
//            }
//        }
        for (i in node.nodeSet) {
            i.number = number++
        }
    }


    // 用于储存非终结符对应的文法内容，方便后续访问处理
    private val ruleMap = HashMap<String, ArrayList<String>>()
    private val firstSet = HashMap<String, HashSet<String>>()
    private lateinit var beginSymbol: String
    private fun analyseForGraph(): DFANode {

        // content[position]表示在position位置的字母在指针之后，如position=1，例子S->A.B
        val d = DFANode(ruleMap, HashSet())
        d.nodeSet.add(d)
        d.itemArrayList.addAll(d.addItems(beginSymbol))
        d.getNextNodes(0)
        d.itemArrayList.apply {
            val t = distinct()
            clear()
            addAll(t)
        }
        numberNode(d)
        return d
    }

    private fun getFirstSetList() {

        // 找到每个非终结符的first集合
        for (item in ruleMap) {
            if (firstSet[item.key].isNullOrEmpty()) {
                firstSet[item.key] = HashSet<String>().apply {
                    addAll(getFirstSet(item.key))
                }
            } else {
                firstSet[item.key]?.addAll(getFirstSet(item.key))
            }
        }
        val sb = StringBuilder()
        for (item in firstSet) {
            sb.append("First[${item.key}]={ ")
            sb.append(item.value.joinToString(" ,"))
            sb.append(" }\n")
        }
        firstSetArea.text = sb.toString()
    }


    private fun getFirstSet(symbol: String): HashSet<String> {
        val s = HashSet<String>()
        for (i in ruleMap[symbol]!!) {
            if (i[0].isUpperCase()) {
                if (i[0].toString() != symbol)
                    s.addAll(getFirstSet(i[0].toString()))
            } else {
                s.add(i[0].toString())
            }
        }
        return s
    }

    /*
    三种规则
    对于非终结符S
    1.若S为开始符号，则follow【S】置入$
    2.在箭头右侧寻找S，且后面有符号，则将后面符号的first集合加入到follow【S】
    3.A->bS,若S后面无符号，或后面的符号为空，则将Follow【A】加入到follow【S】
     */

    private val followSet = HashMap<String, HashSet<String>>()

    private fun getFollowSet() {
        for ((k, _) in ruleMap) {
            if (followSet[k].isNullOrEmpty()) {
                followSet[k] = hashSetOf()
            }
            if (k == beginSymbol) {
                followSet[k]?.add("$")
            }
            for ((nk, nv) in ruleMap) {
                for (i in nv) {
                    for ((index, j) in i.withIndex()) {
                        if (j.toString() == k) {
                            if (index != i.length - 1) {
                                // 符合第二条规则
                                if (i[index + 1].isUpperCase()) {
                                    // 后一个非终结符的first集合加入,同时去除first集合中的空元素
                                    firstSet[i[index + 1].toString()]?.let {
                                        if (it.contains("$") && it.size == 1) {
                                            // 后面的非终结符只包含结束符号，此处符合第三条规则
                                            if (followSet[nk].isNullOrEmpty()) {
                                                followSet[k]?.add(nk)
                                            } else {
                                                followSet[nk]?.let { nit -> followSet[k]?.addAll(nit) }
                                            }
                                        } else {
                                            val tempSet = it.minus("$")
                                            followSet[k]?.addAll(tempSet)
                                        }
                                    }
                                } else {
                                    // 直接加入终结符
                                    followSet[k]?.add(i[index + 1].toString())
                                }
                            } else {
                                // 符合第三条规则
                                if (followSet[nk].isNullOrEmpty()) {
                                    followSet[k]?.add(nk)
                                } else {
                                    followSet[nk]?.let { followSet[k]?.addAll(it) }
                                }
                            }
                        }
                    }
                }
            }
        }
        // 将暂放的follow集合精简
        followSet.map {
            it.value.removeAll(it.value.filter { str ->
                str == it.key
            })
        }
        // 将每个元素中的暂存非终结符对应的follow集合引进，要用迭代器进行删除，否则会报错
        for ((k, v) in followSet) {
            val iterator = v.iterator()
            val tempSet = HashSet<String>()
            while (iterator.hasNext()) {
                val t = iterator.next()[0]
                if (t.isUpperCase()) {
                    iterator.remove()
                    followSet[t.toString()]?.let { tempSet.addAll(it) }
                }
            }
            v.addAll(tempSet)
        }

        val text = firstSetArea.text
        val sb = StringBuilder(text)
        sb.append("\n\n")

        for (item in followSet) {
            sb.append("Follow[${item.key}]={ ")
            sb.append(item.value.joinToString(" ,"))
            sb.append(" }\n")
        }
        firstSetArea.text = sb.toString()
    }


    private fun isConflicting(originNode: DFANode): Boolean {
        for (node in originNode.nodeSet) {
            val nextKeys = node.nextMap.keys
            for (item in node.itemArrayList) {
                if (item.isRestrictItem()) {
                    if ((followSet[item.symbol]?.intersect(nextKeys)?.minus(hashSetOf("$")))?.size!! > 0) {
                        isSLR.text = "否"
                        reason.text = "冲突了！居然是Node${node.number}有规约移进冲突"
                        return true
                    }

                }
            }
        }
        isSLR.text = "是"
        return false
    }


    @FXML
    private fun makeTable(originNode: DFANode) {
        symbolSet.remove("|")
        symbolSet.add("$")
        val table = ArrayList<HashMap<String, Int>>()
        for (i in 0..originNode.nodeSet.size) {
            table.add(hashMapOf())
        }
        for (node in originNode.nodeSet) {
            val restrictSet = node.itemArrayList.filter { it.isRestrictItem() }
            for (c in symbolSet) {
                //c表示下一字符
                if (node.nextMap.containsKey(c)) {
                    node.nextMap[c]?.number?.let { table[node.number].put(c, it) }
                } else {
                    for (i in restrictSet) {
                        if (followSet[i.symbol]?.contains(c) == true) {
                            // 如果c在约束规则对应的follow集合内，标记为约束符-1，接收
                            table[node.number].put(c, -1)
                        }
                    }
                }
            }
        }
        val sb = StringBuilder()
        for ((index, i) in table.withIndex()) {
            if (index > 0) {
                sb.append("\nNode${index}:\n")
                for (c in symbolSet) {
                    if (i.containsKey(c)) {
                        if (i[c] == -1) {
                            sb.append("$c->Restrict\n")
                        } else {
                            sb.append("$c->${i[c]}\n")
                        }
                    } else {
                        sb.append("$c->出错\n")
                    }
                }
            }
        }
        tableArea.text = sb.toString()
    }

}

fun main() {
    val t1 = hashSetOf("1", "2", "3")
    val t2 = hashMapOf("1" to 2)
    println(t2["2"])
}

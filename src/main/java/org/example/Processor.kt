package org.example

import java.lang.StringBuilder

class Processor {
    companion object {
        val keyWordMap = buildByKeys(arrayOf("include", "main", "asm", "auto", "bool", "break", "case", "catch", "char",
                "class", "const", "const cast", "continue", "default", "delete", "do", "double", "enum", "explicit",
                "export", "extern", "false", "float", "for", "friend", "goto", "if", "int", "inline", "long", "mutable",
                "new", "operator", "private", "protected", "public", "register", "reinterpret", "return", "short", "signed", "sizeof", "static", "static_cast", "struct", "switch", "cast", "this", "throw", "true", "try", "typedef", "typeid", "typename", "umon", "unsigned", "using", "virtual", "void", "volatile", "wchar_t", "while", "dynamic_cast", "namespace", "template"))

        val operatorMap = buildByKeys(arrayOf("+", "-", "*", "/", "%", "=", "<", ">", "+=", "-=", "*=", "/=", "%=", "++", "--",
                "==", ">=", "<=", "<<", ">>"))
        val specialMap = buildByKeys(arrayOf("#", ";", ")", "(", "{", "}"))

        private fun buildByKeys(keyArray: Array<String>): HashMap<String, Int> {
            val hMap = HashMap<String, Int>()
            for (i in keyArray) {
                hMap[i] = 1
            }
            return hMap
        }
    }

    private val userDef = HashMap<String, Int>()
    val result = ArrayList<Pair<String, String>>()
    var canMinus = false
    var mark = false
    var allMark = false
    var allMarkCanEnd = false
    fun handleText(text: String) {
        val tempBuilder = StringBuilder()
        for (i in text) {
            if (allMark) {
                tempBuilder.append(i)
                if (i=='*'){
                    allMarkCanEnd = true
                }else if (i!='/'){
                    allMarkCanEnd = false
                }
                if (allMarkCanEnd&&i=='/'){
                    allMark=false
                    val last = result.last().first
                    result.removeAt(result.size - 1)
                    result.add(last + tempBuilder to "注释")
                    mark = false
                    tempBuilder.clear()
                }
            } else {
                if (mark) {
                    //单行注释
                    if (i == '\n') {
                        val last = result.last().first
                        result.removeAt(result.size - 1)
                        result.add(last + tempBuilder to "注释")
                        mark = false
                        tempBuilder.clear()
                    } else {
                        tempBuilder.append(i)
                    }
                } else {
                    //遇到运算符，空格，特殊符号进行处理
                    val isSpecial = specialMap.containsKey(i.toString())
                    val isOperator = operatorMap.containsKey(i.toString())
                    if (isSpecial) {
                        //特殊字符
                        printType(tempBuilder.toString())
                        result.add(i.toString() to "特殊符号")
                        tempBuilder.clear()
                        canMinus = true
                    } else if (isOperator) {
                        //运算符
                        printType(tempBuilder.toString())
                        tempBuilder.clear()

                        //等号之后的“-”号才可能是负号
                        if (operatorMap.containsKey(i.toString())&&i!='-') {
                            canMinus = true
                        }

                        if (canMinus && i == '-') {
                            //纳入数字进行计算
                            tempBuilder.append(i)
                            canMinus = false
                        } else {

                            printType(i.toString())
                        }
                    } else if (i != '\n' && i != ' '&&i!='\t') {
                        //字母或者数字
                        tempBuilder.append(i)
                        canMinus = false
                    } else {
                        //处理标识符或者数字或者串
                        printType(tempBuilder.toString())
                        tempBuilder.clear()
                    }
                }
            }
        }
    }

    private fun printType(temp: String) {
        if (temp != "") {
            when {
                keyWordMap.containsKey(temp) -> {
                    println("$temp is keyWord")
                    result.add(temp to "关键字")
                }
                userDef.containsKey(temp) -> {
                    println("$temp is var")
                    result.add(temp to "标识符")
                }
                operatorMap.containsKey(temp) -> {
                    println("$temp is operator")
                    if (result.isEmpty()) {
                        result.add(temp to "运算符")
                    } else {
                        val last = result.last().first
                        if (last + temp == "//") {
                            result.removeAt(result.size - 1)
                            result.add(last + temp to "注释")
                            mark = true
                        } else if (last + temp == "/*") {
                            result.removeAt(result.size - 1)
                            result.add(last + temp to "注释")
                            allMark = true
                        } else if (operatorMap.containsKey(last) && operatorMap.containsKey(last + temp)) {
                            result.removeAt(result.size - 1)
                            result.add(last + temp to "运算符")
                        } else {
                            result.add(temp to "运算符")
                        }
                    }
                }
                specialMap.containsKey(temp) -> {
                    println("$temp is specialWord")
                    result.add(temp to "特殊符号")
                }
                else -> {
                    when {
                        temp.matches(Regex("[-]?0x[A-Fa-f0-9]*|[-]?[0-9.e]*")) -> {
                            println("$temp is number")
                            result.add(temp to "数")
                        }
                        temp.matches(Regex("""".*?"""")) -> {
                            println("$temp is string")
                            result.add(temp to "串")
                        }
                        else -> {
                            userDef.putIfAbsent(temp, 1)
                            println("$temp is var")
                            result.add(temp to "标识符")
                        }
                    }
                }
            }
        }
    }

}


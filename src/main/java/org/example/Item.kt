package org.example



class Item(val symbol: String, val content: String, var position: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Item

        if (symbol != other.symbol) return false
        if (content != other.content) return false
        if (position != other.position) return false

        return true
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + position
        return result
    }

    fun isRestrictItem():Boolean{
        if (position == content.length) {
            return true
        }
        if (content[position] == '$'){
            return true
        }
        return false
    }
}

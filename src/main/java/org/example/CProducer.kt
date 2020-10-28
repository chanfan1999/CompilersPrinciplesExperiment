package org.example

import java.lang.StringBuilder


class CProducer {
    companion object {
        fun toC(minNodeList: ArrayList<MinDFANode>):String {
            val sb = StringBuilder()
            for (i in minNodeList) {
                sb.append(funCreator(i))
            }
            sb.append("""void main(){
    char t;
    cin>>t;
    A(t);
}""")
            return sb.toString()
        }

        fun funCreator(m: MinDFANode): String {
            val sb = StringBuilder()
            sb.append("""void ${m.name}(char t){
    switch(t){
    """)
            for ((k, v) in m.toStatus) {
                sb.append("""case '${k}':{
        $v(cin.get());
        break;
    }
    """)
            }
            sb.append("""default:{
        cout<<"error";
        break;
        }
    }
}
""")
            return sb.toString()
        }
    }
}
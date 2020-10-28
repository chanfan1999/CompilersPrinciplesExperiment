package org.example

import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.io.FileReader
import java.io.FileWriter


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
    private lateinit var regexText: TextArea

    @FXML
    lateinit var nfaResult: TextArea

    @FXML
    lateinit var dfaResult: TextArea
    @FXML
    lateinit var minDFAResult: TextArea


    @FXML
    fun openFile() {
        file = FileChooser().showOpenDialog(rootLayout.scene.window as Stage)
        file?.apply {
            val fileReader = FileReader(this)
            regexText.text = fileReader.readText()
            fileReader.close()
        }
    }



    @FXML
    fun saveFile() {
        if (file == null) {
            val fileChooser = FileChooser()
            val extFilter = FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt")
            fileChooser.extensionFilters.add(extFilter)
            val s = Stage()
            val file = fileChooser.showSaveDialog(s)
            val fileWriter = FileWriter(file)
            fileWriter.write(regexText.text)
            fileWriter.close()
        } else {
            val text = regexText.text
            val fileWriter = FileWriter(file)
            fileWriter.write(text)
            fileWriter.close()

        }
        tell("保存成功")
    }

    @FXML
    fun toNFA() {
        val status = NFAUtils.countStatusNumber(regexText.text)
        val resultList = NFAUtils.regexToNFA(regexText.text)
        val nfaText = NFAUtils.getNFATableText(resultList, status)
        val n = StringBuilder()
        nfaText.forEach {
            n.append(it)
            n.append('\n')
        }
        nfaResult.text = n.toString()
    }

    @FXML
    fun toDFA() {
        val status = NFAUtils.countStatusNumber(regexText.text)
        val nfaList = NFAUtils.regexToNFA(regexText.text)
        val endNFANodeNumber = NFAUtils.getEndNodeNumber(nfaList)
        val dfaList = DFAUtils.toDFA(nfaList, status,endNFANodeNumber)
        val t = DFAUtils.getDFATableText(dfaList)
        dfaResult.text = t
    }

    @FXML
    fun minimizeDFA(){
        val status = NFAUtils.countStatusNumber(regexText.text)
        val nfaList = NFAUtils.regexToNFA(regexText.text)
        val endNFANodeNumber = NFAUtils.getEndNodeNumber(nfaList)
        val dfaList = DFAUtils.toDFA(nfaList, status,endNFANodeNumber)
        val t = DFAUtils.minimizeDFA(dfaList,status)
        val tex = DFAUtils.getMinDFAText(t)
        minDFAResult.text = tex
    }
}
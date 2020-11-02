package org.example

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.FlowPane
import java.io.FileReader
import java.nio.charset.Charset

class Controller {
    @FXML
    private lateinit var compileButton: Button

    @FXML
    private lateinit var sourceArea: TextArea

    @FXML
    private lateinit var resultArea:TextArea

    @FXML
    private lateinit var view:FlowPane

    @FXML
    fun onCompileButtonClick(event: ActionEvent?) {
        val resourceText = sourceArea.text+'\n'
        val processor = Processor()
        processor.handleText(resourceText)
        val sb = StringBuilder()
        for (i in processor.result){
            sb.append("${i.first}      ")
            sb.append("${i.second}\n")
        }
        resultArea.text = sb.toString()
    }

    @FXML
    fun onCompressButtonPress(){
        val resourceText = sourceArea.text+'\n'
        val processor = Processor()
        val sb = StringBuilder()
        processor.handleText(resourceText)
        processor.apply {
            for (i in result){
                if (Processor.keyWordMap.containsKey(i.first)){
                    sb.append(i.first+" ")
                }else{
                    if (i.second != "注释")
                        sb.append(i.first)
                }
            }
        }
        resultArea.text = sb.toString()
    }

    @FXML
    fun drag(){
        view.setOnDragDone {
            it.dragboard.files.stream().toString()
        }
    }

    @FXML
    fun handleDragOver(dragEvent: DragEvent){
        if (dragEvent.dragboard.hasFiles()){
            dragEvent.acceptTransferModes(*TransferMode.ANY)
        }
    }

    @FXML
    fun handleDrag(dragEvent: DragEvent){
        val f = dragEvent.dragboard.files
        val reader = FileReader(f[0],Charset.forName("UTF-8"))
        val text = reader.readText()
        sourceArea.text = text
        reader.close()
    }
}
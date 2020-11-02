package org.example

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class App : Application() {
    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("test.fxml"))
        primaryStage.title = "Hello World"
        primaryStage.scene = Scene(root,900.0,450.0)
        primaryStage.show()
    }
}
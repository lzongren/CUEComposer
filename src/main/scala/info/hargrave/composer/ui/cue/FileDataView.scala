package info.hargrave.composer.ui.cue

import javafx.geometry.{Insets => JFXInsets}

import info.hargrave.composer._
import info.hargrave.composer.ui.Editable
import info.hargrave.composer.util.CUEUtilities._
import jwbroek.cuelib.FileData

import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{ComboBox, Label, TextField}
import scalafx.scene.layout.GridPane

/**
 * Provides the editing view for filedata
 */
class FileDataView(fileData: FileData) extends GridPane with Editable {

    // View Setup ------------------------------------------------------------------------------------------------------

    padding = new Insets(new JFXInsets(5, 5, 5, 5))
    hgap    = 10.0
    vgap    = 5.0

    // File Name -------------------------------------------------------------------------------------------------------

    val fileNameLabel   = new Label(t"ui.fd_view.file_name")
    val fileField       = new TextField {
        text = fileData.file.orNull
        text.onChange({fileData.file = Option(text.value)})
    }

    this.addRow(0, fileNameLabel, fileField)

    // File Type -------------------------------------------------------------------------------------------------------

    val fileTypeLabel   = new Label(t"ui.fd_view.file_type")
    val typeField       = new ComboBox[String] {
        this.editable.bind(editableProperty)
        items = ObservableBuffer(FileEntry.FileTypes)
        value = fileData.fileType.orNull
        value.onChange({fileData.fileType = Option(value.value)})
    }

    this.addRow(1, fileTypeLabel, typeField)

}
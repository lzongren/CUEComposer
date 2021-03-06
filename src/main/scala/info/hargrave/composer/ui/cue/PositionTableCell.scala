package info.hargrave.composer.ui.cue

import javafx.scene.control.{TableCell => JFXTableCell}

import info.hargrave.composer.ui.cue.PositionTableCell.JFXImpl
import info.hargrave.composer.util.CUEUtilities._
import jwbroek.cuelib.Position

import scalafx.Includes._
import scalafx.delegate.SFXDelegate
import scalafx.scene.control.TableCell
import scalafx.scene.input.{KeyCode, KeyEvent}

/**
 * Provides a TableCell that allows for the display and editing of a position
 */
class PositionTableCell[S](override val delegate: JFXImpl[S] = new JFXImpl[S])
        extends TableCell[S, Position] with SFXDelegate[JFXImpl[S]]
object PositionTableCell {

    private val ZeroPosition = new Position(0, 0, 0)

    /**
     * Custom delegate class that provides the implementation of startEdit, cancelEdit, and updateItem
     * for displaying positions
     *
     * @tparam S item data type
     */
    final class JFXImpl[S] extends JFXTableCell[S, Position] {

        private var position: Option[Position] = None
        private var positionView: Option[PositionView] = None

        override def startEdit(): Unit = if(isEditable && getTableView.isEditable && getTableColumn.isEditable) {
            super.startEdit()

            if(positionView.isEmpty) {
                positionView = Some(PositionView.copyFrom(position.get))

                positionView.get.onKeyReleased = { (event: KeyEvent) =>
                    event.code match {
                        case KeyCode.ENTER =>
                            commitEdit(positionView.get.underlying)
                        case KeyCode.ESCAPE =>
                            cancelEdit()
                        case _ =>
                    }
                }

                positionView.get.editableProperty.bind(editableProperty)
            }

            setText(null)
            setGraphic(positionView.get)
        }

        override def cancelEdit(): Unit = {
            setGraphic(null)
            setText(position.getOrElse(ZeroPosition).formatted)
            super.cancelEdit()
        }

        override def updateItem(item: Position, empty: Boolean): Unit = empty match {
            case true =>
                setText(null)
                setGraphic(null)
                position = None
                super.updateItem(item, empty)
            case false =>
                position = Some(item)
                setGraphic(null)
                setText(item.formatted)
                super.updateItem(item, empty)
        }
    }
}

package io.github.rednesto.musicshelf.utils

import javafx.css.Styleable
import javafx.scene.control.TableColumnBase
import javafx.scene.control.TableView
import javafx.scene.control.skin.TableViewSkinBase
import java.lang.reflect.InaccessibleObjectException

fun Styleable.addClass(styleClass: String) = this.styleClass.addIfAbsent(styleClass)

fun Styleable.addClasses(vararg styleClasses: String) = styleClasses.forEach { this.styleClass.addIfAbsent(it) }

fun Styleable.removeClasses(vararg styleClasses: String) = this.styleClass.removeAll(styleClasses)

fun getWidthOfAllColumns(tableView: TableView<*>): Double =
        tableView.columns.fold(0.0) { width, col -> width + col.width }

/**
 * Tries to resize all the columns of [tableView].
 *
 * This method uses reflection to access a non-public API of JavaFX
 * which can only be accessed if the following `java` argument is used
 * when launching the application:
 *
 * `--add-opens javafx.controls/javafx.scene.control.skin=musicshelf`
 *
 * If the access is denied this method will do nothing.
 */
fun tryResizeColumnsToContent(tableView: TableView<*>) = try {
    val resizeMethod = Class.forName("javafx.scene.control.skin.TableSkinUtils")
            .getDeclaredMethod("resizeColumnToFitContent",
                    TableViewSkinBase::class.java, TableColumnBase::class.java, Integer.TYPE)
    resizeMethod.isAccessible = true
    tableView.columns.forEach { column -> resizeMethod(null, tableView.skin, column, -1) }
} catch (e: InaccessibleObjectException) {
}

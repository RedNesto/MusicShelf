package io.github.rednesto.musicshelf.utils

import javafx.css.Styleable

fun Styleable.addClass(styleClass: String) = this.styleClass.addIfAbsent(styleClass)

fun Styleable.addClasses(vararg styleClasses: String) = styleClasses.forEach { this.styleClass.addIfAbsent(it) }

fun Styleable.removeClasses(vararg styleClasses: String) = this.styleClass.removeAll(styleClasses)

package dev.borisochieng.sketchpad.ui.screens.drawingboard.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions

data class PathProperties(
	val id: String = "",
	val alpha: Float = 1f,
	val color: Color = Color.Black,
	val eraseMode: Boolean = false,
	val shapeMode: Boolean = false,
	val textMode: Boolean = false,
	val start: Offset = Offset.Zero,
	val end: Offset = Offset.Zero,
	val strokeWidth: Float = 10f
)

data class ShapeProperties(
	val id: String = "",
	val shape: ShapeOptions,
	val offset: Offset = Offset(100f, 100f),
	val scale: Float = 1f,
	val rotation: Float = 0f
)

data class TextProperties(
	val id: String = "",
	val text: String = "",
	val offset: Offset = Offset(100f, 100f),
	val scale: Float = 1f,
	val rotation: Float = 0f
)

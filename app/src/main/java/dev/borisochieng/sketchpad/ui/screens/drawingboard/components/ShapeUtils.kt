package dev.borisochieng.sketchpad.ui.screens.drawingboard.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.rectangle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Circle
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Oval
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Pentagon
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Rectangle
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Square
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Star
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Triangle
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.ShapeProperties
import java.util.UUID.randomUUID
import kotlin.math.max

@Composable
fun MoveableShapeBox(
	properties: ShapeProperties,
	active: Boolean,
	modifier: Modifier = Modifier,
	onRemove: (ShapeProperties) -> Unit,
	onFinish: (ShapeProperties) -> Unit = {},
	onUpdate: (ShapeProperties) -> Unit = {}
) {
	var offset by remember { mutableStateOf(properties.offset) }
	var rotation by remember { mutableFloatStateOf(properties.rotation) }
	var scale by remember { mutableFloatStateOf(properties.scale) }
	var shapeIsActive by remember(active) { mutableStateOf(active) }

	Column(
		modifier = modifier
			.offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
			.graphicsLayer {
				scaleX = scale
				scaleY = scale
				rotationZ = rotation
			}
			.pointerInput(shapeIsActive) {
				if (!shapeIsActive) return@pointerInput
				detectTransformGestures { _, pan, zoom, rotationChange ->
					offset += pan
					rotation += rotationChange
					scale *= zoom
				}
			},
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		if (shapeIsActive) {
			ActionButtons(
				onDone = {
					shapeIsActive = false
					val shapeIsNew = properties.id.isEmpty()
					val shapeProperties = ShapeProperties(
						id = if (shapeIsNew) randomUUID().toString() else properties.id,
						shape = properties.shape, offset = offset,
						scale = scale, rotation = rotation
					)
					if (shapeIsNew) onFinish(shapeProperties) else onUpdate(shapeProperties)
				},
				onRemove = {
					shapeIsActive = false
					onRemove(properties)
				}
			)
		}
		CustomShape(
			shapeOption = properties.shape,
			borderColor = Color.Black,
			modifier = Modifier.clickable(!shapeIsActive) { shapeIsActive = true }
		)
	}
}

@Composable
fun CustomShape(
	shapeOption: ShapeOptions,
	borderColor: Color,
	modifier: Modifier = Modifier,
	shapeSize: Dp = 120.dp
) {
	val degrees = when (shapeOption) {
		Star -> -90f
		Pentagon -> -90f
		Triangle -> -90f
		else -> 0f
	}
	val polygon = remember {
		when (shapeOption) {
			Circle -> RoundedPolygon.circle()
			Oval -> RoundedPolygon.circle()
			Rectangle -> {
				RoundedPolygon.rectangle(
					rounding = CornerRounding(0.05f)
				)
			}
			Square -> {
				RoundedPolygon.rectangle(
					rounding = CornerRounding(0.05f)
				)
			}
			Star -> {
				RoundedPolygon.star(
					numVerticesPerRadius = shapeOption.numVertices,
					rounding = CornerRounding(0.05f)
				)
			}
			else -> {
				RoundedPolygon(
					numVertices = shapeOption.numVertices,
					rounding = CornerRounding(0.05f)
				)
			}
		}
	}
	val shape = remember(polygon) {
		RoundedPolygonShape(polygon)
	}
	val shapeModifier = when (shapeOption) {
		Oval -> modifier.size(shapeSize / 2, shapeSize)
		Rectangle -> modifier.size(shapeSize, shapeSize / 2)
		else -> modifier.size(shapeSize)
	}

	Box(
		modifier = shapeModifier
			.rotate(degrees)
			.clip(shape)
			.border(4.dp, borderColor, shape)
	)
}

@Composable
private fun ActionButtons(
	onDone: () -> Unit,
	onRemove: () -> Unit
) {
	Row(
		modifier = Modifier
			.width(120.dp)
			.padding(vertical = 6.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Rounded.Check,
			contentDescription = "Done",
			modifier = Modifier.clickable { onDone() }
		)
		Spacer(Modifier.weight(1f))
		Icon(
			imageVector = Icons.Rounded.Close,
			contentDescription = "Remove",
			modifier = Modifier.clickable { onRemove() }
		)
	}
}

enum class ShapeOptions(val numVertices: Int) {
	Circle(1),
	Oval(1),
	Triangle(3),
	Rectangle(4),
	Square(4),
	Star(5),
	Pentagon(5),
	Hexagon(6),
	Octagon(8)
}

@Preview(showBackground = true)
@Composable
private fun CustomShapePreview() {
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		CustomShape(Circle, Color.Black)
		CustomShape(Oval, Color.Black)
		CustomShape(Triangle, Color.Black)
		CustomShape(Rectangle, Color.Black)
		CustomShape(Square, Color.Black)
		CustomShape(Star, Color.Black)
		CustomShape(Pentagon, Color.Black)
//		CustomShape(Hexagon, Color.Black)
//		CustomShape(Octagon, Color.Black)
	}
}

private class RoundedPolygonShape(
	private val polygon: RoundedPolygon,
	private var matrix: Matrix = Matrix()
) : Shape {
	private var path = Path()

	override fun createOutline(
		size: Size,
		layoutDirection: LayoutDirection,
		density: Density
	): Outline {
		path.rewind()
		path = polygon.toPath().asComposePath()
		matrix.reset()

		val bounds = polygon.getBounds()
		val maxDimension = max(bounds.width, bounds.height)
		matrix.scale(size.width / maxDimension, size.height / maxDimension)
		matrix.translate(-bounds.left, -bounds.top)

		path.transform(matrix)
		return Outline.Generic(path)
	}
}

private fun RoundedPolygon.getBounds() = calculateBounds().let {
	Rect(it[0], it[1], it[2], it[3])
}

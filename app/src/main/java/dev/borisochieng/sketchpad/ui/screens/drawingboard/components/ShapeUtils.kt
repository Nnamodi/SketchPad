package dev.borisochieng.sketchpad.ui.screens.drawingboard.components

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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Circle
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Hexagon
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Octagon
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Pentagon
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Square
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions.Triangle
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.ShapeProperties
import java.util.UUID.randomUUID

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
			}
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
			shape = properties.shape,
			borderColor = Color.Black,
			modifier = Modifier.clickable(!shapeIsActive) { shapeIsActive = true }
		)
	}
}

@Composable
fun CustomShape(
	shape: ShapeOptions,
	borderColor: Color,
	modifier: Modifier = Modifier,
	shapeSize: Dp = 120.dp
) {
	val degrees = when (shape) {
		Triangle -> 30f
		Square -> 45f
		Pentagon -> -18f
		else -> 0f
	}

	Box(
		modifier = modifier.rotate(degrees),
		contentAlignment = Alignment.Center
	) {
		when (shape) {
			Circle -> {
				Circle(Modifier.size(shapeSize), borderColor)
				Circle(Modifier.size(shapeSize - 4.dp))
			}
//			Star -> {
//				Star(Modifier.size(shapeSize.dp), borderColor)
//				Star(Modifier.size(shapeSize - 4.dp))
//			}
			else -> {
				Polygons(shape.numVertices, Modifier.size(shapeSize), borderColor)
				Polygons(shape.numVertices, Modifier.size(shapeSize - 4.dp))
			}
		}
	}
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

@Composable
private fun Circle(
	modifier: Modifier = Modifier,
	borderColor: Color = Color.White
) {
	Box(
		modifier = modifier
			.drawWithCache {
				val shape = RoundedPolygon.circle(
					radius = size.minDimension / 2,
					centerX = size.width / 2,
					centerY = size.height / 2
				)
				val shapePath = shape
					.toPath()
					.asComposePath()
				onDrawBehind {
					drawPath(shapePath, borderColor)
				}
			}
	)
}

@Composable
private fun Polygons(
	numVertices: Int,
	modifier: Modifier = Modifier,
	borderColor: Color = Color.White
) {
	Box(
		modifier = modifier
			.drawWithCache {
				val shape = RoundedPolygon(
					numVertices = numVertices,
					radius = size.minDimension / 2,
					centerX = size.width / 2,
					centerY = size.height / 2,
				)
				val shapePath = shape
					.toPath()
					.asComposePath()
				onDrawBehind {
					drawPath(shapePath, borderColor)
				}
			}
	)
}

@Composable
private fun Star(
	modifier: Modifier = Modifier,
	borderColor: Color = Color.White
) {
	Box(
		modifier = modifier
			.drawWithCache {
				val shape = RoundedPolygon.star(
					numVerticesPerRadius = 6,
					radius = size.minDimension / 2,
					rounding = CornerRounding(0.1f),
					centerX = size.width / 2,
					centerY = size.height / 2
				)
				val shapePath = shape
					.toPath()
					.asComposePath()
				onDrawBehind {
					drawPath(shapePath, borderColor)
				}
			}
	)
}

enum class ShapeOptions(val numVertices: Int) {
	Circle(1),
//	Star(2),
	Triangle(3),
	Square(4),
	Pentagon(5),
	Hexagon(6),
	Octagon(8)
}

@Preview(showBackground = true)
@Composable
private fun CustomShapePreview() {
	Column {
		CustomShape(Circle, Color.Black)
		CustomShape(Triangle, Color.Black)
		CustomShape(Square, Color.Black)
		CustomShape(Pentagon, Color.Black)
//		CustomShape(Star, Color.Black)
		CustomShape(Hexagon, Color.Black)
		CustomShape(Octagon, Color.Black)
	}
}

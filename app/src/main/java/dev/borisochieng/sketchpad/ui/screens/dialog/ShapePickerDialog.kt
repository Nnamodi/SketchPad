package dev.borisochieng.sketchpad.ui.screens.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.CustomShape
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions
import dev.borisochieng.sketchpad.utils.Extensions.transformList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapePickerDialog(
	onSelected: (ShapeOptions) -> Unit,
	onDismiss: () -> Unit
) {
	BasicAlertDialog(
		onDismissRequest = onDismiss,
		modifier = Modifier
			.padding(16.dp)
			.clip(AlertDialogDefaults.shape)
			.background(MaterialTheme.colorScheme.background)
			.padding(20.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState()),
		) {
			val shapes = ShapeOptions.entries.transformList(rowCount = 3)
			repeat(shapes.size) { index ->
				val rowShapes = shapes[index]

				ShapeRow(
					shapes = rowShapes,
					modifier = Modifier.fillMaxWidth(),
					onSelected = {
						onSelected(it)
						onDismiss()
					}
				)
			}
		}
	}
}

@Composable
private fun ShapeRow(
	shapes: List<ShapeOptions>,
	modifier: Modifier = Modifier,
	onSelected: (ShapeOptions) -> Unit
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(bottom = 8.dp),
		horizontalArrangement = Arrangement.SpaceAround,
		verticalAlignment = Alignment.CenterVertically
	) {
		repeat(shapes.size) { index ->
			val shape = shapes[index]

			Box(
				Modifier
					.clip(MaterialTheme.shapes.large)
					.clickable { onSelected(shape) }
					.padding(6.dp),
				contentAlignment = Alignment.Center
			) {
				CustomShape(
					shape = shape,
					borderColor = Color.Black,
					shapeSize = 60.dp
				)
			}
		}
	}
}

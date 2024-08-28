package dev.borisochieng.sketchpad.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.PathProperties
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.ShapeProperties
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.TextProperties
import java.util.Date

class TypeConverter {

	@TypeConverter
	fun fromDate(date: Date?) = date?.time

	@TypeConverter
	fun toDate(millisSinceEpoch: Long?): Date? {
		return millisSinceEpoch?.let {
			Date(it)
		}
	}

	@TypeConverter
	fun fromPaths(paths: List<PathProperties>): String {
		val gson = Gson()
		return gson.toJson(paths)
	}

	@TypeConverter
	fun toPaths(pathJson: String): List<PathProperties> {
		val gson = Gson()
		val type = object : TypeToken<List<PathProperties>>() {}.type
		return gson.fromJson(pathJson, type)
	}

	@TypeConverter
	fun fromShapes(shapes: List<ShapeProperties>): String {
		val gson = Gson()
		return gson.toJson(shapes)
	}

	@TypeConverter
	fun toShapes(shapesJson: String): List<ShapeProperties> {
		val gson = Gson()
		val type = object : TypeToken<List<ShapeProperties>>() {}.type
		return gson.fromJson(shapesJson, type)
	}

	@TypeConverter
	fun fromTexts(texts: List<TextProperties>): String {
		val gson = Gson()
		return gson.toJson(texts)
	}

	@TypeConverter
	fun toTexts(textsJson: String): List<TextProperties> {
		val gson = Gson()
		val type = object : TypeToken<List<TextProperties>>() {}.type
		return gson.fromJson(textsJson, type)
	}

}
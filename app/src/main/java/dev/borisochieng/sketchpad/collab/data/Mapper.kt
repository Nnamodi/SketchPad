package dev.borisochieng.sketchpad.collab.data

import dev.borisochieng.sketchpad.collab.data.models.DBOffset
import dev.borisochieng.sketchpad.collab.data.models.DBPathProperties
import dev.borisochieng.sketchpad.collab.data.models.DBSketch
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties
import dev.borisochieng.sketchpad.utils.Extensions.formatDate
import dev.borisochieng.sketchpad.utils.Extensions.toHexString

fun PathProperties.toDBPathProperties(): DBPathProperties {
    return DBPathProperties(
        alpha = alpha,
        color = color.toHexString(),
        eraseMode = eraseMode,
        start = DBOffset(x = start.x, y = start.y),
        end = DBOffset(x = end.x, y= end.y ),
        strokeWidth = strokeWidth
    )
}

fun Sketch.toDBSketch(): DBSketch {
    return DBSketch(
        id = id,
        dateCreated = dateCreated.formatDate(),
        lastModified = lastModified.formatDate(),
        title = name,
        paths = pathList.map { it.toDBPathProperties() },
    )
}
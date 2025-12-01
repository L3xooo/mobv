package eu.mcomputing.mobv.zadanie.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.turf.TurfTransformation
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GeofenceUtils {

    companion object {

        const val RADIUS_SIZE = 1000;

        fun zoomToLocation(mapboxMap: MapboxMap, latitude: Double, longitude: Double, zoom: Double = 14.0) {
            val point = Point.fromLngLat(longitude, latitude)
            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(point)
                    .zoom(zoom)
                    .build()
            )
        }

        fun drawGeofenceCircle(
            style: Style,
            latitude: Double,
            longitude: Double,
            radiusMeters: Double,
            sourceId: String = "circle-source",
            layerId: String = "circle-layer",
            fillColor: String = "#3366FF",
            fillOpacity: Double = 0.2
        ) {
            Log.d("GeofenceUtils", "Draw circle $latitude $longitude $radiusMeters")
            val center = Point.fromLngLat(longitude, latitude)
            val circlePolygon: Polygon = TurfTransformation.circle(center, radiusMeters, 64, "meters")

            style.addSource(
                geoJsonSource(sourceId) {
                    feature(Feature.fromGeometry(circlePolygon))
                }
            )

            style.addLayer(
                fillLayer(layerId, sourceId) {
                    this.fillColor(fillColor)
                    this.fillOpacity(fillOpacity)
                }
            )
        }

        fun createUserMarker(
            context: Context,
            pointAnnotationManager: PointAnnotationManager?,
            point: Point,
            userId: String?,
            imageUrl: String?,
            @DrawableRes placeholderRes: Int
        ) {
            Glide.with(context)
                .asBitmap()
                .load(imageUrl ?: placeholderRes)
                .error(placeholderRes)
                .placeholder(placeholderRes)
                .override(88, 88)
                .centerCrop()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val annotationOptions = PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage(resource)
                            .withData(JsonObject().apply { addProperty("uid", userId) })

                        pointAnnotationManager?.create(annotationOptions)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        // optional cleanup
                    }
                })
        }



        fun generateRandomPointInRadius(center: Point, radiusMeters: Double): Point {
            // Convert radius from meters to degrees
            val radiusInDegrees = radiusMeters / 111_320f // 1 degree ≈ 111.32 km

            // Generate random distance and angle
            val random = java.util.Random()
            val randomDistance = radiusInDegrees * sqrt(random.nextDouble())
            val randomAngle = 2 * Math.PI * random.nextDouble()

            val deltaLat = randomDistance * cos(randomAngle)
            val deltaLon = randomDistance * sin(randomAngle) / cos(Math.toRadians(center.latitude()))

            val newLat = center.latitude() + deltaLat
            val newLon = center.longitude() + deltaLon

            return Point.fromLngLat(newLon, newLat)
        }
    }
}
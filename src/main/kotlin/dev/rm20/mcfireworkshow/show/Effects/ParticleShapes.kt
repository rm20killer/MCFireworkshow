package dev.rm20.mcfireworkshow.show.Effects

import com.destroystokyo.paper.ParticleBuilder
import dev.rm20.mcfireworkshow.helpers.ParticleHelper
import dev.rm20.mcfireworkshow.helpers.ParticleInfo
import org.bukkit.Location
import org.bukkit.Particle.DustOptions
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ParticleShapes {
    companion object {
        private fun lerp(start: Double, end: Double, progress: Double): Double {
            return start + (end - start) * progress
        }

        private fun rotate(point: Vector, yaw: Double, pitch: Double, roll: Double): Vector {
            val yawRad = Math.toRadians(yaw)
            val pitchRad = Math.toRadians(pitch)
            val rollRad = Math.toRadians(roll)

            var rotatedPoint = point.clone()
            rotatedPoint = rotatedPoint.rotateAroundY(yawRad)
            rotatedPoint = rotatedPoint.rotateAroundX(pitchRad)
            rotatedPoint = rotatedPoint.rotateAroundZ(rollRad)

            return rotatedPoint
        }

        //Shapes
        fun drawLine(particleTypeString: String, pos1: Location, pos2: Location, density: Double) {
            val vector = pos2.toVector().subtract(pos1.toVector())
            val length = vector.length()
            vector.normalize()

            val step = 1.0 / density
            var currentDistance = 0.0
            val particleInfo: ParticleInfo = ParticleHelper.getParticleInfoFromString(particleTypeString)
                ?: throw IllegalArgumentException("Invalid particle: $particleTypeString")
            val particle = particleInfo.getParticle()
            //val particleBuilder = ParticleBuilder(particle).count(1).extra(0.0).force(true).receivers(256)
//            if (particleInfo.data != null) {
//                val dustOptions = particleInfo.data as DustOptions
//                particleBuilder.color(dustOptions.color, dustOptions.size)
//            }
            while (currentDistance < length) {
                val currentPoint = pos1.clone().add(vector.clone().multiply(currentDistance))
                val particleBuilder =
                    ParticleBuilder(particle).location(currentPoint).count(1).extra(0.0).force(true).receivers(256)
                if (particleInfo.data != null) {
                    val dustOptions = particleInfo.data as DustOptions
                    particleBuilder.color(dustOptions.color, dustOptions.size)
                }
                particleBuilder.spawn()
                currentDistance += step
            }
        }

        fun drawAnimatedLine(
            particleString: String, pos1: Location, pos2: Location, density: Double, startProgress: Double, endProgress: Double
        ) {
            val vector = pos2.toVector().subtract(pos1.toVector())
            val startPoint = pos1.clone().add(vector.clone().multiply(startProgress))
            val endPoint = pos1.clone().add(vector.clone().multiply(endProgress))

            // Draw the line segment
            drawLine(particleString, startPoint.toLocation(pos1.world), endPoint.toLocation(pos1.world), density)
        }

        fun drawArch(particleTypeString: String, pos1: Location, pos2: Location, height: Double, density: Double) {
            val distance = pos1.distance(pos2)
            val step = 1.0 / (distance * density)

            val particleInfo: ParticleInfo = ParticleHelper.getParticleInfoFromString(particleTypeString)
                ?: throw IllegalArgumentException("Invalid particle: $particleTypeString")
            val particle = particleInfo.getParticle()

            var t = 0.0
            while (t <= 1.0) {
                val x = (1 - t) * pos1.x + t * pos2.x
                val z = (1 - t) * pos1.z + t * pos2.z
                val y = (1 - t) * pos1.y + t * pos2.y + height * (1 - (2 * t - 1) * (2 * t - 1))

                val particleLocation = Location(pos1.world, x, y, z)
                val particleBuilder =
                    ParticleBuilder(particle).location(particleLocation).count(1).extra(0.0).force(true).receivers(256)

                if (particleInfo.data != null) {
                    val dustOptions = particleInfo.data as DustOptions
                    particleBuilder.color(dustOptions.color, dustOptions.size)
                }
                particleBuilder.spawn()
                t += step
            }
        }

        fun drawAnimatedArch(
            particleTypeString: String,
            pos1: Location,
            pos2: Location,
            height: Double,
            density: Double,
            startProgress: Double,
            endProgress: Double
        ) {
            val distance = pos1.distance(pos2)
            val step = 1.0 / (distance * density)

            val particleInfo: ParticleInfo = ParticleHelper.getParticleInfoFromString(particleTypeString)
                ?: throw IllegalArgumentException("Invalid particle: $particleTypeString")
            val particle = particleInfo.getParticle()

            var t = startProgress
            while (t <= endProgress) {
                val x = (1 - t) * pos1.x + t * pos2.x
                val z = (1 - t) * pos1.z + t * pos2.z
                val y = (1 - t) * pos1.y + t * pos2.y + height * (1 - (2 * t - 1) * (2 * t - 1))

                val particleLocation = Location(pos1.world, x, y, z)
                val particleBuilder =
                    ParticleBuilder(particle).location(particleLocation).count(1).extra(0.0).force(true).receivers(256)

                if (particleInfo.data != null) {
                    val dustOptions = particleInfo.data as DustOptions
                    particleBuilder.color(dustOptions.color, dustOptions.size)
                }
                particleBuilder.spawn()
                t += step
            }
        }

        fun drawCircle(
            particleTypeString: String,
            centre: Location,
            radius: Double,
            rotation: Vector,
            density: Double,
            startAngle: Double = 0.0,
            endAngle: Double = 2 * Math.PI
        ) {
            if (radius <= 0) return // No circle to draw if radius is zero or negative
            val angularStep = 1.0 / (radius * density)
            val particleInfo: ParticleInfo = ParticleHelper.getParticleInfoFromString(particleTypeString)
                ?: throw IllegalArgumentException("Invalid particle: ${particleTypeString}")
            val particle = particleInfo.getParticle()

            var angle = startAngle
            while (angle < endAngle) {
                val x = radius * cos(angle)
                val y = radius * sin(angle)

                var point = Vector(x, y, 0.0)
                point = rotate(point, rotation.x, rotation.y, rotation.z)

                val particleLocation = centre.clone().add(point)
                val particleBuilder =
                    ParticleBuilder(particle).location(particleLocation).count(1).extra(0.0).force(true).receivers(256)

                if (particleInfo.data != null) {
                    val dustOptions = particleInfo.data as DustOptions
                    particleBuilder.color(dustOptions.color, dustOptions.size)
                }
                particleBuilder.spawn()
                angle += angularStep
            }
        }

        fun drawAnimatedCircle(
            particleTypeString: String,
            centre: Location,
            radius: Double,
            rotation: Vector,
            endRadius: Double?,
            endRotation: Vector?,
            density: Double,
            progress: Double,
            startCircumferenceProgress: Double,
            endCircumferenceProgress: Double
        ) {
            val currentRadius = if (endRadius != null) lerp(radius, endRadius, progress) else radius
            val currentRotation = if (endRotation != null) {
                val yaw = lerp(rotation.x, endRotation.x, progress)
                val pitch = lerp(rotation.y, endRotation.y, progress)
                val roll = lerp(rotation.z, endRotation.z, progress)
                Vector(yaw, pitch, roll)
            } else {
                rotation
            }

            drawCircle(
                particleTypeString,
                centre,
                currentRadius,
                currentRotation,
                density,
                startAngle = 2 * Math.PI * startCircumferenceProgress,
                endAngle = 2 * Math.PI * endCircumferenceProgress
            )
        }

        fun drawSphere(particleTypeString: String, centre: Location, radius: Double, density: Double) {
            if (radius <= 0) return
            val particleInfo = ParticleHelper.getParticleInfoFromString(particleTypeString) ?: return

            val points = (density * radius * radius).toInt()
            for (i in 0 until points) {
                val theta = 2 * PI * (i.toDouble() / points)
                val phi = Math.acos(1 - 2 * (i.toDouble() / points))

                val x = radius * sin(phi) * cos(theta)
                val y = radius * sin(phi) * sin(theta)
                val z = radius * cos(phi)

                val particleLocation = centre.clone().add(Vector(x, y, z))
                val particleBuilder =
                    ParticleBuilder(particleInfo.getParticle()).location(particleLocation).count(1).extra(0.0)
                        .force(true).receivers(256).data(particleInfo.data)
                particleBuilder.spawn()
            }
        }

        fun drawAnimatedSphere(
            particleTypeString: String,
            centre: Location,
            radius: Double,
            endRadius: Double?,
            density: Double,
            progress: Double
        ) {
            val currentRadius = if (endRadius != null) lerp(radius, endRadius, progress) else radius
            drawSphere(particleTypeString, centre, currentRadius, density)
        }

        fun drawHemisphere(
            particleTypeString: String,
            centre: Location,
            radius: Double,
            rotation: Vector,
            density: Double
        ) {
            if (radius <= 0) return
            val particleInfo = ParticleHelper.getParticleInfoFromString(particleTypeString) ?: return
            val points = (density * radius * radius).toInt()

            for (i in 0 until points) {
                // Generate points in the upper hemisphere (phi from 0 to PI/2)
                val theta = 2 * PI * (i.toDouble() / points)
                val phi = (PI / 2) * (i.toDouble() / points)

                val x = radius * sin(phi) * cos(theta)
                val y = radius * cos(phi) // Y is up for the base hemisphere
                val z = radius * sin(phi) * sin(theta)

                var point = Vector(x, y, z)
                point = rotate(point, rotation.x, rotation.y, rotation.z)

                val particleLocation = centre.clone().add(point)
                val particleBuilder =
                    ParticleBuilder(particleInfo.getParticle()).location(particleLocation).count(1).extra(0.0)
                        .force(true).receivers(256).data(particleInfo.data)
                particleBuilder.spawn()
            }
        }

        fun drawAnimatedHemisphere(
            particleTypeString: String,
            centre: Location,
            radius: Double,
            rotation: Vector,
            endRadius: Double?,
            density: Double,
            progress: Double
        ) {
            val currentRadius = if (endRadius != null) lerp(radius, endRadius, progress) else radius
            drawHemisphere(particleTypeString, centre, currentRadius, rotation, density)
        }

        fun drawCube(particleTypeString: String, centre: Location, scale: Vector, rotation: Vector, density: Double) {
            val vertices = Array(8) { Vector() }
            val halfScale = scale.clone().multiply(0.5)

            // Define the 8 vertices of the cube
            for (i in 0..7) {
                val x = if (i and 1 == 0) -halfScale.x else halfScale.x
                val y = if (i and 2 == 0) -halfScale.y else halfScale.y
                val z = if (i and 4 == 0) -halfScale.z else halfScale.z
                vertices[i] = rotate(Vector(x, y, z), rotation.x, rotation.y, rotation.z).add(centre.toVector())
            }

            // Define the 12 edges
            val edges = arrayOf(
                0 to 1, 1 to 3, 3 to 2, 2 to 0, // Bottom face
                4 to 5, 5 to 7, 7 to 6, 6 to 4, // Top face
                0 to 4, 1 to 5, 2 to 6, 3 to 7  // Side edges
            )

            // Draw each edge
            for ((start, end) in edges) {
                drawLine(
                    particleTypeString,
                    vertices[start].toLocation(centre.world),
                    vertices[end].toLocation(centre.world),
                    density
                )
            }
        }

        fun drawAnimatedCube(
            particleTypeString: String,
            centre: Location,
            scale: Vector,
            rotation: Vector,
            endScale: Vector?,
            density: Double,
            progress: Double
        ) {
            val currentScale = if (endScale != null) {
                Vector(
                    lerp(scale.x, endScale.x, progress),
                    lerp(scale.y, endScale.y, progress),
                    lerp(scale.z, endScale.z, progress)
                )
            } else {
                Vector(scale.x, scale.y, scale.z)
            }
            drawCube(particleTypeString, centre, currentScale, rotation, density)
        }
    }

}
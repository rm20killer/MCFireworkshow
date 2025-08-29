package dev.rm20.mcfireworkshow.show.camera

import CameraMovement
import Location
import Rotation
import org.bukkit.util.Vector

object Interpolation {

    // --- LOCATION INTERPOLATION ---

    fun linear(p0: CameraMovement, p1: CameraMovement, t: Double): Location {
        val start = p0.location
        val end = p1.location
        val x = start.x + (end.x - start.x) * t
        val y = start.y + (end.y - start.y) * t
        val z = start.z + (end.z - start.z) * t
        return Location(x, y, z)
    }

    fun easeInOut(p0: CameraMovement, p1: CameraMovement, t: Double): Location {
        val start = p0.location
        val end = p1.location
        val easedT = t * t * (3.0 - 2.0 * t)
        val x = start.x + (end.x - start.x) * easedT
        val y = start.y + (end.y - start.y) * easedT
        val z = start.z + (end.z - start.z) * easedT
        return Location(x, y, z)
    }

    fun easeIn(p0: CameraMovement, p1: CameraMovement, t: Double): Location {
        val start = p0.location
        val end = p1.location
        val easedT = t * t
        val x = start.x + (end.x - start.x) * easedT
        val y = start.y + (end.y - start.y) * easedT
        val z = start.z + (end.z - start.z) * easedT
        return Location(x, y, z)
    }

    fun easeOut(p0: CameraMovement, p1: CameraMovement, t: Double): Location {
        val start = p0.location
        val end = p1.location
        val easedT = 1.0 - (1.0 - t) * (1.0 - t)
        val x = start.x + (end.x - start.x) * easedT
        val y = start.y + (end.y - start.y) * easedT
        val z = start.z + (end.z - start.z) * easedT
        return Location(x, y, z)
    }

    fun catmullRom(p0: Vector, p1: Vector, p2: Vector, p3: Vector, t: Double): Location {
        val t2 = t * t
        val t3 = t2 * t
        val x = 0.5 * ((2 * p1.x) + (-p0.x + p2.x) * t + (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * t2 + (-p0.x + 3 * p1.x - 3 * p2.x + p3.x) * t3)
        val y = 0.5 * ((2 * p1.y) + (-p0.y + p2.y) * t + (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * t2 + (-p0.y + 3 * p1.y - 3 * p2.y + p3.y) * t3)
        val z = 0.5 * ((2 * p1.z) + (-p0.z + p2.z) * t + (2 * p0.z - 5 * p1.z + 4 * p2.z - p3.z) * t2 + (-p0.z + 3 * p1.z - 3 * p2.z + p3.z) * t3)
        return Location(x, y, z)
    }

    // --- ROTATION INTERPOLATION ---

    fun linearRotation(p0: CameraMovement, p1: CameraMovement, t: Double): Rotation {
        val start = p0.rotation
        val end = p1.rotation
        // Normalize yaw difference to prevent spinning the long way around
        var yawDiff = end.yaw - start.yaw
        if (yawDiff > 180) yawDiff -= 360
        if (yawDiff < -180) yawDiff += 360

        val yaw = start.yaw + yawDiff * t
        val pitch = start.pitch + (end.pitch - start.pitch) * t
        val roll = start.roll + (end.roll - start.roll) * t
        return Rotation(yaw, pitch, roll)
    }

    fun easeInOutRotation(p0: CameraMovement, p1: CameraMovement, t: Double): Rotation {
        val easedT = t * t * (3.0 - 2.0 * t)
        return linearRotation(p0, p1, easedT)
    }

    fun easeInRotation(p0: CameraMovement, p1: CameraMovement, t: Double): Rotation {
        val easedT = t * t
        return linearRotation(p0, p1, easedT)
    }

    fun easeOutRotation(p0: CameraMovement, p1: CameraMovement, t: Double): Rotation {
        val easedT = 1.0 - (1.0 - t) * (1.0 - t)
        return linearRotation(p0, p1, easedT)
    }
}

package app.wimt.cheese.service.io

/**
 * Conmmand codes between the service and the client
 */
class Command {

    object Request {
        const val SHUTDOWN = 112
        const val CONNECT = 113
        const val DISCONNECT = 114
        const val MARKER_ADD = 117
        const val MARKER_REMOVE = 118
        const val MONITOR = 119
        const val CLEAR_NEAR = 120

    }

    object Response {
        const val LOCATION = 224
        const val LOCATION_ERROR = 225
        const val MARKERS = 226
        const val MARKERS_NEAR = 229
        const val MARKERS_NOT_NEAR = 220 //out of scope but could hide notifications that are shown that are nolonger valid
        const val MARKER_ADDED = 221
        const val MARKER_REMOVED = 222
    }


}